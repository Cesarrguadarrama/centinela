-- Tuning after observing normal traffic: with 15-minute windows and minIncoming=8, randomly
-- popular accounts in the synthetic pool triggered the mule rule (~2.4% of transactions).
-- Tightened thresholds keep the injected attack (12 deposits, 90% dispersal in seconds) clearly
-- detectable while silencing organic noise. Applied live via the rules API first; this migration
-- makes the values durable for fresh installs.
UPDATE rules
SET params = '{"minIncoming": 10, "minDistinctSenders": 6, "dispersalRatio": 0.7, "windowMinutes": 10}'::jsonb
WHERE type = 'MULE_ACCOUNT';
