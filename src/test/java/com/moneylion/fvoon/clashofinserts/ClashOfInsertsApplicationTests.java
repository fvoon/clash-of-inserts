package com.moneylion.fvoon.clashofinserts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ClashOfInsertsApplicationTests {

	@Autowired
	private FundService fundService;
	@Autowired
	private FundOptionRepository fundOptionRepository;

	private static final String USER_ID = "user123";

	@Test
	void triggerFailsToPreventDuplicateInsertUnderConcurrency() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch latch = new CountDownLatch(1);

		Callable<FundOption> task = () -> {
			latch.await();
			try {
				FundOption fundOption = FundOption.builder()
						.userId(USER_ID)
						.type(FundOptionTypes.ACH.getValue())
						.details(FundDetails.builder()
								.bankRoutingNumber("1111")
								.bankAccountNumber("2222")
								.build())
						.identifier("2222")
						.isDeleted(false)
						.isDisabled(false)
						.build();
				return fundService.persistFundOption(fundOption);
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}
		};

		Future<FundOption> insert1 = executor.submit(task);
		Future<FundOption> insert2 = executor.submit(task);

		latch.countDown(); // release both threads simultaneously

		FundOption result1 = insert1.get();
		FundOption result2 = insert2.get();

		List<FundOption> inserted = fundOptionRepository.findAllByUserId(USER_ID);

		System.out.println("inserted count: " + inserted.size());
		inserted.forEach(f -> System.out.println("fundOption ID: " + f.getId()));

		assertTrue(inserted.size() <= 1, "trigger was bypassed under concurrency");

		executor.shutdownNow();
	}

	@Test
	void performance() throws Exception {
		int threadCount = 20;
		int iterationsPerThread = 10;

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(1);
		List<Future<Long>> futures = new ArrayList<>();

		for (int i = 0; i < threadCount; i++) {
			int threadId = i;
			futures.add(executor.submit(() -> {
				latch.await();
				long start = System.nanoTime();
				for (int j = 0; j < iterationsPerThread; j++) {
					try {
						String account = "acct-" + j;
						String identifier = threadId + "-" + j;

						FundOption fundOption = FundOption.builder()
								.userId("user-" + threadId)
								.type(FundOptionTypes.ACH.getValue())
								.details(FundDetails.builder()
										.bankRoutingNumber("1111")
										.bankAccountNumber(account)
										.build())
								.identifier(identifier) // so trigger condition behaves differently
								.isDeleted(false)
								.isDisabled(false)
								.build();

						fundService.persistFundOption(fundOption);
					} catch (Exception e) {
						// Can optionally check e.toString().contains("duplicate") or log
						System.out.println("Insert failed for user-" + threadId + ": " + e.getMessage());
					}
				}
				return System.nanoTime() - start;
			}));
		}

		long testStart = System.currentTimeMillis();
		latch.countDown(); // release all threads
		long totalNanos = 0;

		for (Future<Long> future : futures) {
			totalNanos += future.get(); // wait for all threads
		}

		long elapsedMillis = System.currentTimeMillis() - testStart;
		List<FundOption> all = fundOptionRepository.findAll();
		System.out.println("Inserted FundOptions: " + all.size());
		System.out.println("Total time (ms): " + elapsedMillis);
		System.out.println("Avg time per thread (ms): " + (totalNanos / 1_000_000 / threadCount));

		executor.shutdown();
	}

}
