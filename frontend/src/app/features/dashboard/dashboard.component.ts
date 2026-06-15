import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription, interval, startWith } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AlertStreamService } from '../../core/alert-stream.service';
import { maskClabe, money, scoreColor } from '../../core/format';
import { MetricsOverview } from '../../core/models';
import { Bar, BarChartComponent } from '../../shared/bar-chart.component';
import { LineChartComponent, LinePoint } from '../../shared/line-chart.component';
import { SeverityBadgeComponent } from '../../shared/severity-badge.component';

@Component({
  selector: 'cen-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    LineChartComponent,
    BarChartComponent,
    SeverityBadgeComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  protected readonly stream = inject(AlertStreamService);

  protected readonly overview = signal<MetricsOverview | null>(null);
  protected readonly pulse = signal<LinePoint[]>([]);
  protected readonly scoreBars = signal<Bar[]>([]);

  protected readonly liveAlerts = this.stream.liveAlerts;
  protected readonly connected = this.stream.connected;

  protected readonly maskClabe = maskClabe;
  protected readonly money = money;
  protected readonly scoreColor = scoreColor;

  protected readonly detectionRate = computed(() => {
    const o = this.overview();
    if (!o || o.totalTransactions === 0) return '0';
    return ((o.totalAlerts / o.totalTransactions) * 100).toFixed(2);
  });

  private poll?: Subscription;

  ngOnInit(): void {
    this.stream.connect();
    this.poll = interval(5000)
      .pipe(startWith(0))
      .subscribe(() => this.refresh());
  }

  ngOnDestroy(): void {
    this.poll?.unsubscribe();
  }

  private refresh(): void {
    this.api.overview().subscribe((o) => this.overview.set(o));
    this.api.transactionsPerMinute(30).subscribe((buckets) =>
      this.pulse.set(
        buckets.map((b) => ({
          label: b.minute,
          a: b.transactions,
          b: b.alerts,
        })),
      ),
    );
    this.api.scoreDistribution().subscribe((dist) => {
      const byBucket = new Map(dist.map((d) => [d.bucket, d.count]));
      const bars: Bar[] = [];
      for (let b = 0; b <= 90; b += 10) {
        bars.push({
          label: `${b}+`,
          value: byBucket.get(b) ?? 0,
          color: scoreColor(b),
        });
      }
      this.scoreBars.set(bars);
    });
  }
}
