package mx.centinela.bootstrap.api.dto;

/** Histogram bar: transactions whose score falls in [bucket, bucket+10). */
public record ScoreBucket(int bucket, long count) {}
