package com.moneylion.fvoon.clashofinserts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

}
