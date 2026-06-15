import { Component, computed, input } from '@angular/core';
import { Severity } from '../core/models';
import { SEVERITY_COLOR } from '../core/format';

@Component({
  selector: 'cen-severity',
  standalone: true,
  template: `<span class="badge" [style.color]="color()" [style.borderColor]="color()">{{
    severity()
  }}</span>`,
  styles: [
    `
      .badge {
        padding: 0.1rem 0.5rem;
        border: 1px solid;
        border-radius: 999px;
        font-size: 0.72rem;
        font-weight: 600;
        letter-spacing: 0.02em;
      }
    `,
  ],
})
export class SeverityBadgeComponent {
  readonly severity = input.required<Severity>();
  protected readonly color = computed(() => SEVERITY_COLOR[this.severity()]);
}
