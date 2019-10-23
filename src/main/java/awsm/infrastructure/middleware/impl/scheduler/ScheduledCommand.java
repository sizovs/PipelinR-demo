package awsm.infrastructure.middleware.impl.scheduler;

import static com.google.common.base.Preconditions.checkState;
import static com.machinezoo.noexception.Exceptions.sneak;
import static java.time.ZoneOffset.UTC;
import static jooq.tables.ScheduledCommand.SCHEDULED_COMMAND;

import awsm.infrastructure.middleware.Command;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import jooq.tables.records.ScheduledCommandRecord;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jooq.DSLContext;

class ScheduledCommand {

  enum Status {
    PENDING, DONE
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  private int ranTimes;

  @Nullable
  private LocalDateTime lastRunTime;

  private final LocalDateTime creationDate;

  private Status status;

  private Command command;

  ScheduledCommand(Command command) {
    this.creationDate = LocalDateTime.now(UTC);
    this.status = Status.PENDING;
    this.command = command;
  }

  ScheduledCommand(ScheduledCommandRecord rec) {
    this.ranTimes = rec.getRanTimes();
    this.lastRunTime = rec.getLastRunTime();
    this.creationDate = rec.getCreationDate();
    this.status = Status.valueOf(rec.getStatus());
    this.command = sneak().get(() -> mapper.readValue(rec.getCommand(), Command.class));
  }

  void saveNew(DSLContext dsl) {
    dsl
      .insertInto(SCHEDULED_COMMAND,
          SCHEDULED_COMMAND.RAN_TIMES,
          SCHEDULED_COMMAND.LAST_RUN_TIME,
          SCHEDULED_COMMAND.CREATION_DATE,
          SCHEDULED_COMMAND.STATUS,
          SCHEDULED_COMMAND.COMMAND)
      .values(ranTimes, lastRunTime, creationDate, status.name(), sneak().get(() -> mapper.writeValueAsString(command)))
      .execute();
  }

  CompletableFuture executeIn(Executor executor) {
    checkState(status == Status.PENDING, "Cannot execute work that is not %s", status);
    this.ranTimes++;
    this.lastRunTime = LocalDateTime.now(UTC);
    return CompletableFuture
        .runAsync(() -> this.command.execute(), executor)
        .thenRun(() -> this.status = Status.DONE);
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SIMPLE_STYLE);
  }

}