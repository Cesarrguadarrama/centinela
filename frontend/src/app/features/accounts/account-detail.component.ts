import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { maskClabe, money, scoreColor } from '../../core/format';
import { AccountSummary, Page, TransactionView } from '../../core/models';

@Component({
  selector: 'cen-account-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-detail.component.html',
  styleUrl: './account-detail.component.scss',
})
export class AccountDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(ApiService);

  protected readonly clabe = toSignal(
    this.route.paramMap.pipe(map((p) => p.get('clabe') ?? '')),
    { initialValue: '' },
  );

  protected readonly summary = signal<AccountSummary | null>(null);
  protected readonly transactions = signal<Page<TransactionView> | null>(null);

  protected readonly maskClabe = maskClabe;
  protected readonly money = money;
  protected readonly scoreColor = scoreColor;

  constructor() {
    const clabe = this.clabe();
    if (clabe) {
      this.api.accountSummary(clabe).subscribe((s) => this.summary.set(s));
      this.api.accountTransactions(clabe, 0, 50).subscribe((t) => this.transactions.set(t));
    }
  }

  direction(tx: TransactionView): 'out' | 'in' {
    return tx.sourceClabe === this.clabe() ? 'out' : 'in';
  }
}
