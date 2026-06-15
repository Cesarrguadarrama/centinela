import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AccountSummary,
  Alert,
  MetricsOverview,
  MinuteBucket,
  Page,
  Rule,
  ScoreBucket,
  TransactionView,
} from './models';

/** Typed gateway to the Centinela REST API. Components never build URLs themselves. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api';

  searchAlerts(filters: {
    status?: string;
    severity?: string;
    clabe?: string;
    page?: number;
    size?: number;
  }): Observable<Page<Alert>> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(filters)) {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<Page<Alert>>(`${this.base}/alerts`, { params });
  }

  triageAlert(id: string, action: 'review' | 'false-positive'): Observable<Alert> {
    return this.http.post<Alert>(`${this.base}/alerts/${id}/${action}`, {});
  }

  // --- Rules ---
  listRules(): Observable<Rule[]> {
    return this.http.get<Rule[]>(`${this.base}/rules`);
  }

  createRule(rule: Omit<Rule, 'id'>): Observable<Rule> {
    return this.http.post<Rule>(`${this.base}/rules`, rule);
  }

  updateRule(id: string, rule: Omit<Rule, 'id'>): Observable<Rule> {
    return this.http.put<Rule>(`${this.base}/rules/${id}`, rule);
  }

  // --- Metrics ---
  overview(): Observable<MetricsOverview> {
    return this.http.get<MetricsOverview>(`${this.base}/metrics/overview`);
  }

  transactionsPerMinute(minutes = 30): Observable<MinuteBucket[]> {
    return this.http.get<MinuteBucket[]>(
      `${this.base}/metrics/transactions-per-minute`,
      { params: new HttpParams().set('minutes', minutes) },
    );
  }

  scoreDistribution(): Observable<ScoreBucket[]> {
    return this.http.get<ScoreBucket[]>(`${this.base}/metrics/score-distribution`);
  }

  // --- Accounts ---
  accountSummary(clabe: string, hours = 24): Observable<AccountSummary> {
    return this.http.get<AccountSummary>(`${this.base}/accounts/${clabe}/summary`, {
      params: new HttpParams().set('hours', hours),
    });
  }

  accountTransactions(
    clabe: string,
    page = 0,
    size = 20,
  ): Observable<Page<TransactionView>> {
    return this.http.get<Page<TransactionView>>(
      `${this.base}/accounts/${clabe}/transactions`,
      { params: new HttpParams().set('page', page).set('size', size) },
    );
  }

  // --- Generator control (port 8081 in the composed deployment, proxied to /generator) ---
  startTraffic(): Observable<unknown> {
    return this.http.post(`/generator/api/traffic/start`, {});
  }

  injectScenario(scenario: string): Observable<unknown> {
    return this.http.post(`/generator/api/scenarios/${scenario}`, {});
  }
}
