import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ApiService } from './core/api.service';
import { AlertStreamService } from './core/alert-stream.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly api = inject(ApiService);
  protected readonly stream = inject(AlertStreamService);
  protected readonly busy = signal<string | null>(null);

  protected readonly scenarios = ['mule', 'smurfing', 'velocity', 'off_hours'];

  inject_(scenario: string): void {
    this.busy.set(scenario);
    this.api.injectScenario(scenario).subscribe({
      next: () => setTimeout(() => this.busy.set(null), 1500),
      error: () => this.busy.set(null),
    });
  }
}
