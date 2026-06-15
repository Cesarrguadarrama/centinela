import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { maskClabe, money, scoreColor } from '../../core/format';
import { Alert, AlertStatus, Page, Severity } from '../../core/models';
import { SeverityBadgeComponent } from '../../shared/severity-badge.component';

@Component({
  selector: 'cen-alerts',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, SeverityBadgeComponent],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss',
})
export class AlertsComponent implements OnInit {
  private readonly api = inject(ApiService);

  protected readonly page = signal<Page<Alert> | null>(null);
  protected readonly loading = signal(false);

  protected status = '';
  protected severity = '';
  protected clabe = '';
  protected pageIndex = 0;
  protected readonly size = 20;

  protected readonly severities: Severity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  protected readonly statuses: AlertStatus[] = ['NEW', 'REVIEWED', 'FALSE_POSITIVE'];

  protected readonly maskClabe = maskClabe;
  protected readonly money = money;
  protected readonly scoreColor = scoreColor;

  ngOnInit(): void {
    this.search();
  }

  search(resetPage = true): void {
    if (resetPage) {
      this.pageIndex = 0;
    }
    this.loading.set(true);
    this.api
      .searchAlerts({
        status: this.status,
        severity: this.severity,
        clabe: this.clabe.trim(),
        page: this.pageIndex,
        size: this.size,
      })
      .subscribe((p) => {
        this.page.set(p);
        this.loading.set(false);
      });
  }

  triage(alert: Alert, action: 'review' | 'false-positive'): void {
    this.api.triageAlert(alert.id, action).subscribe(() => this.search(false));
  }

  totalPages(): number {
    const p = this.page();
    return p ? Math.max(1, Math.ceil(p.totalItems / p.size)) : 1;
  }

  go(delta: number): void {
    const next = this.pageIndex + delta;
    if (next >= 0 && next < this.totalPages()) {
      this.pageIndex = next;
      this.search(false);
    }
  }
}
