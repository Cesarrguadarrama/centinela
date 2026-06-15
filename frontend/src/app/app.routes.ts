import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    title: 'Centinela — Dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'alerts',
    title: 'Centinela — Alertas',
    loadComponent: () =>
      import('./features/alerts/alerts.component').then((m) => m.AlertsComponent),
  },
  {
    path: 'accounts/:clabe',
    title: 'Centinela — Cuenta',
    loadComponent: () =>
      import('./features/accounts/account-detail.component').then(
        (m) => m.AccountDetailComponent,
      ),
  },
  {
    path: 'rules',
    title: 'Centinela — Reglas',
    loadComponent: () =>
      import('./features/rules/rules.component').then((m) => m.RulesComponent),
  },
  { path: '**', redirectTo: 'dashboard' },
];
