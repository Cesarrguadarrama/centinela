import { CommonModule } from '@angular/common';
import { Component, computed, input } from '@angular/core';

export interface Bar {
  label: string;
  value: number;
  color: string;
}

/** Inline-SVG histogram — used for the score distribution. */
@Component({
  selector: 'cen-bar-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <svg [attr.viewBox]="'0 0 ' + W + ' ' + H" class="chart">
      @for (bar of layout(); track bar.label) {
        <rect
          [attr.x]="bar.x"
          [attr.y]="bar.y"
          [attr.width]="bar.w"
          [attr.height]="bar.h"
          [attr.fill]="bar.color"
          rx="2"
        />
        <text [attr.x]="bar.cx" [attr.y]="H - 4" text-anchor="middle" class="lbl">
          {{ bar.label }}
        </text>
        @if (bar.value > 0) {
          <text [attr.x]="bar.cx" [attr.y]="bar.y - 4" text-anchor="middle" class="val">
            {{ bar.value }}
          </text>
        }
      }
    </svg>
  `,
  styles: [
    `
      :host {
        display: block;
      }
      .chart {
        width: 100%;
        height: 200px;
      }
      .lbl {
        fill: #8b949e;
        font-size: 11px;
      }
      .val {
        fill: #e6edf3;
        font-size: 11px;
      }
    `,
  ],
})
export class BarChartComponent {
  readonly bars = input.required<Bar[]>();

  protected readonly W = 600;
  protected readonly H = 200;

  protected readonly layout = computed(() => {
    const bars = this.bars();
    const max = Math.max(1, ...bars.map((b) => b.value));
    const slot = this.W / Math.max(1, bars.length);
    const barW = slot * 0.7;
    const top = 20;
    const bottom = 24;
    return bars.map((b, i) => {
      const h = (b.value / max) * (this.H - top - bottom);
      return {
        ...b,
        x: i * slot + (slot - barW) / 2,
        w: barW,
        h,
        y: this.H - bottom - h,
        cx: i * slot + slot / 2,
      };
    });
  });
}
