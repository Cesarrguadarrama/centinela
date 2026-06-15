/** Mirrors the backend API DTOs (see backend bootstrap api/dto). */

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type AlertStatus = 'NEW' | 'REVIEWED' | 'FALSE_POSITIVE';
export type RuleType =
  | 'SUB_THRESHOLD_AMOUNT'
  | 'VELOCITY'
  | 'OFF_HOURS'
  | 'MULE_ACCOUNT'
  | 'SMURFING';

export interface Alert {
  id: string;
  transactionId: string;
  ruleName: string;
  severity: Severity;
  explanation: string;
  status: AlertStatus;
  createdAt: string;
  sourceClabe: string;
  destinationClabe: string;
  amount: number;
  transactionScore: number;
}

/** Live SSE payload — same shape the feed renders, plus it arrives push-style. */
export interface AlertStreamEvent {
  alertId: string;
  transactionId: string;
  ruleName: string;
  severity: Severity;
  explanation: string;
  sourceClabe: string;
  destinationClabe: string;
  amount: number;
  transactionScore: number;
  createdAt: string;
}

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
}

export interface Rule {
  id: string;
  type: RuleType;
  name: string;
  description: string;
  enabled: boolean;
  severity: Severity;
  weight: number;
  params: Record<string, unknown>;
}

export interface MetricsOverview {
  totalTransactions: number;
  totalAlerts: number;
  alertsBySeverity: Record<string, number>;
  alertsByStatus: Record<string, number>;
  averageScore: number;
}

export interface MinuteBucket {
  minute: string;
  transactions: number;
  alerts: number;
}

export interface ScoreBucket {
  bucket: number;
  count: number;
}

export interface AccountSummary {
  clabe: string;
  windowHours: number;
  sentCount: number;
  sentTotal: number;
  receivedCount: number;
  receivedTotal: number;
  alertCount: number;
  maxScore: number;
}

export interface TransactionView {
  id: string;
  sourceClabe: string;
  destinationClabe: string;
  amount: number;
  concept: string;
  timestamp: string;
  score: number;
}
