import { CommonModule } from '@angular/common';
import { Component, computed, input } from '@angular/core';

export interface LinePoint {
  label: string;
  a: number;
  b: number;
}

/**
 * Lightweight dual-series line chart drawn as inline SVG — no charting dependency. Series A is the
 * transaction pulse, series B the alert count, sharing one Y scale.
 */
@Component({
  selector: 'cen-line-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <svg [attr.viewBox]="'0 0 ' + W + ' ' + H" preserveAspectRatio="none" class="chart">
      <polyline [attr.points]="pathA()" class="series-a" />
      <polyline [attr.points]="pathB()" class="series-b" />
    </svg>
    <div class="legend">
      <span class="swatch a"></span> {{ labelA() }}
      <span class="swatch b"></span> {{ labelB() }}
      <span class="peak">pico {{ peak() }}/min</span>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
      }
      .chart {
        width: 100%;
        height: 180px;
        background: #0d1117;
        border-radius: 8px;
      }
      polyline {
        fill: none;
        stroke-width: 2;
        vector-effect: non-scaling-stroke;
      }
      .series-a {
        stroke: #58a6ff;
      }
      .series-b {
        stroke: #f85149;
      }
      .legend {
        display: flex;
        gap: 0.5rem;
        align-items: center;
        font-size: 0.8rem;
        color: #8b949e;
        margin-top: 0.4rem;
      }
      .swatch {
        width: 12px;
        height: 3px;
        display: inline-block;
      }
      .swatch.a {
        background: #58a6ff;
      }
      .swatch.b {
        background: #f85149;
      }
      .peak {
        margin-left: auto;
      }
    `,
  ],
})
export class LineChartComponent {
  readonly points = input.required<LinePoint[]>();
  readonly labelA = input('Transacciones');
  readonly labelB = input('Alertas');

  protected readonly W = 600;
  protected readonly H = 180;

  protected readonly peak = computed(() =>
    Math.max(1, ...this.points().map((p) => p.a)),
  );

  protected readonly pathA = computed(() => this.line((p) => p.a));
  protected readonly pathB = computed(() => this.line((p) => p.b));

  private line(pick: (p: LinePoint) => number): string {
    const pts = this.points();
    if (pts.length === 0) {
      return '';
    }
    const max = Math.max(1, ...pts.map((p) => Math.max(p.a, p.b)));
    const dx = this.W / Math.max(1, pts.length - 1);
    return pts
      .map((p, i) => `${(i * dx).toFixed(1)},${(this.H - (pick(p) / max) * (this.H - 10)).toFixed(1)}`)
      .join(' ');
  }
}
