package prototype.deltalake;

import io.delta.standalone.DeltaLog;
import io.delta.standalone.Operation;
import io.delta.standalone.OptimisticTransaction;
import io.delta.standalone.actions.AddFile;
import io.delta.standalone.actions.Action;

import io.delta.standalone.actions.Metadata;
import io.delta.standalone.types.StringType;
import io.delta.standalone.types.StructField;
import io.delta.standalone.types.StructType;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.avro.AvroParquetWriter;

//import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//@QuarkusTest
public class DeltaLakeTests  {

    @Test
    public void createBoundedDeltaSourceAllColumns() throws Exception{
        Schema schema = SchemaBuilder
                .record("MyRecord")
                .namespace("mynamespace")
                .fields().requiredString("myfield")
                .endRecord();

        ParquetWriter<GenericRecord> writer = AvroParquetWriter.
                <GenericRecord>builder(new Path("delta/file.parquet"))
                .withSchema(schema)
                .build();

        GenericRecord record = new GenericData.Record(schema);
        record.put("myfield", "myvalue");
        writer.write(record);
        long size = writer.getDataSize();
        System.out.println("SIZE: "+size);
        writer.close();

        DeltaLog log = DeltaLog.forTable(new Configuration(), "delta");
        List<Action> actions = List.of(new AddFile("file.parquet", new HashMap<String, String>(), size, System.currentTimeMillis(), true, null, null));
        OptimisticTransaction txn = log.startTransaction();
        Metadata metaData = txn.metadata()
                .copyBuilder()
                .partitionColumns(new ArrayList<String>())
                .schema(new StructType()
                        .add(new StructField("myfield", new StringType(), true))).build();
        txn.updateMetadata(metaData);
        txn.commit(actions, new Operation(Operation.Name.CREATE_TABLE), "myproject");
    }
}
