package com.cloudera.sa.copybook.mapreduce;

import com.cloudera.sa.copybook.common.Constants;
import com.cloudera.sa.copybook.common.TestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CopyBookInputFormatTest {

  private TestUtils testUtils;

  public static RecordReader getRecordReader(String cobolLocation,
                                             String datafileLocation,
                                             String delimiter,
                                             int fileFormat) throws IOException, InterruptedException {
    Configuration conf = new Configuration(false);
    conf.set("fs.default.name", "file:///");
    conf.set(Constants.COPYBOOK_INPUTFORMAT_CBL_HDFS_PATH_CONF, cobolLocation);
    conf.set(Constants.COPYBOOK_INPUTFORMAT_FIELD_DELIMITER, delimiter);
    conf.set(Constants.COPYBOOK_INPUTFORMAT_FILE_STRUCTURE, Integer.toString(fileFormat));

    File testFile = new File(datafileLocation);
    Path path = new Path(testFile.getAbsoluteFile().toURI());
    FileSplit split = new FileSplit(path, 0, testFile.length(), null);

    InputFormat inputFormat = ReflectionUtils
        .newInstance(CopybookInputFormat.class, conf);
    TaskAttemptContext context = new TaskAttemptContextImpl(conf,
        new TaskAttemptID());

    RecordReader reader = inputFormat.createRecordReader(split, context);
    reader.initialize(split, context);

    return reader;
  }

  @Before
  public void setup() throws Exception {
    testUtils = TestUtils.getInstance();
    testUtils.createTestFiles();
  }

  @After
  public void teardown() {
    testUtils.removeTestFiles();
  }

  @Test
  public void testFixedRecordReader() throws IOException, InterruptedException {
    RecordReader reader = getRecordReader(testUtils.getCobolFileLocation(),
        testUtils.getTestFixedLengthFileLocation(), "0x01",
        net.sf.JRecord.Common.Constants.IO_FIXED_LENGTH);

    int counter = 0;
    while (reader.nextKeyValue()) {
      counter++;
      System.out
          .println(reader.getCurrentKey() + "::\t" + reader.getCurrentValue());
    }
    assertEquals(testUtils.getTestDataLength(), counter);
  }

  @Test
  public void testVbRecordReader() throws IOException, InterruptedException {
    RecordReader reader = getRecordReader(testUtils.getCobolFileLocation(),
        testUtils.getTestVbFileLocation(), "0x01",
        net.sf.JRecord.Common.Constants.IO_VB);

    int counter = 0;
    while (reader.nextKeyValue()) {
      counter++;
      System.out
          .println(reader.getCurrentKey() + "::\t" + reader.getCurrentValue());
    }
    assertEquals(testUtils.getTestDataLength(), counter);
  }

}
