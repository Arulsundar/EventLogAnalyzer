import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;

public class Consumer implements Runnable,Closeable {
	RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
	static BulkProcessor bulkProcessor;

	@Override
	public void run() {
		BulkProcessor.Listener listener = new BulkProcessor.Listener() {
			@Override
			public void beforeBulk(long executionId, BulkRequest request) {
				int numberOfActions = request.numberOfActions();
				System.out.printf("\n Executing bulk %d  with %d requests", executionId, numberOfActions);
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
				if (response.hasFailures()) {
					System.out.printf("Bulk %d executed with failures", executionId);
				} else {
					System.out.printf("\n Bulk %d completed in {} milliseconds", executionId);
				}
			}

			@Override
			public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
				System.out.printf("\n Failed to execute bulk", failure);
			}
		};
		bulkProcessor = BulkProcessor.builder((request, bulkListener) -> {
			client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);
		}, listener).setBulkActions(50).setFlushInterval(TimeValue.timeValueMinutes(1)).build();

		while (true) {

			Properties val = WinLog.queue.take();
			IndexRequest indexRequest = new IndexRequest("winlogs").source("msg", val);
			bulkProcessor.add(indexRequest);
		}

	}

	@Override
	public void close() throws IOException {
        bulkProcessor.flush();		
	}

}
