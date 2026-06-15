import { Injectable, NgZone, OnDestroy, inject, signal } from '@angular/core';
import { AlertStreamEvent } from './models';

/**
 * Wraps the SSE alert feed in a signal. EventSource fires outside Angular's zone, so updates are
 * marshalled back in with NgZone.run to keep change detection honest. Reconnection is automatic
 * (EventSource built-in); we just surface connection state for the UI.
 */
@Injectable({ providedIn: 'root' })
export class AlertStreamService implements OnDestroy {
  private readonly zone = inject(NgZone);
  private source?: EventSource;

  readonly connected = signal(false);
  /** Most recent alerts first, capped so the live feed never grows unbounded. */
  readonly liveAlerts = signal<AlertStreamEvent[]>([]);

  private static readonly MAX_LIVE = 100;

  connect(): void {
    if (this.source) {
      return;
    }
    const source = new EventSource('/api/stream/alerts');
    this.source = source;

    source.addEventListener('open', () => this.zone.run(() => this.connected.set(true)));
    source.addEventListener('error', () => this.zone.run(() => this.connected.set(false)));
    source.addEventListener('alert', (event) => {
      const parsed: AlertStreamEvent = JSON.parse((event as MessageEvent).data);
      this.zone.run(() =>
        this.liveAlerts.update((current) =>
          [parsed, ...current].slice(0, AlertStreamService.MAX_LIVE),
        ),
      );
    });
  }

  disconnect(): void {
    this.source?.close();
    this.source = undefined;
    this.connected.set(false);
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
