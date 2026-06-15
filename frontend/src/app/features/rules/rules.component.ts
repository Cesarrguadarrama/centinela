import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Rule } from '../../core/models';
import { SeverityBadgeComponent } from '../../shared/severity-badge.component';

@Component({
  selector: 'cen-rules',
  standalone: true,
  imports: [CommonModule, FormsModule, SeverityBadgeComponent],
  templateUrl: './rules.component.html',
  styleUrl: './rules.component.scss',
})
export class RulesComponent implements OnInit {
  private readonly api = inject(ApiService);

  protected readonly rules = signal<Rule[]>([]);
  protected readonly editing = signal<Rule | null>(null);
  protected readonly saving = signal(false);

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.api.listRules().subscribe((r) => this.rules.set(r));
  }

  edit(rule: Rule): void {
    // Deep copy so cancel discards changes; params edited as pretty JSON text
    this.editing.set({ ...rule, params: { ...rule.params } });
  }

  cancel(): void {
    this.editing.set(null);
  }

  paramsText(rule: Rule): string {
    return JSON.stringify(rule.params, null, 2);
  }

  onParamsText(rule: Rule, text: string): void {
    try {
      rule.params = JSON.parse(text);
    } catch {
      // keep last valid value; save will use whatever parsed last
    }
  }

  save(): void {
    const rule = this.editing();
    if (!rule) return;
    this.saving.set(true);
    const { id, ...body } = rule;
    this.api.updateRule(id, body).subscribe({
      next: () => {
        this.saving.set(false);
        this.editing.set(null);
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }

  toggle(rule: Rule): void {
    const { id, ...body } = rule;
    this.api.updateRule(id, { ...body, enabled: !rule.enabled }).subscribe(() => this.load());
  }
}
