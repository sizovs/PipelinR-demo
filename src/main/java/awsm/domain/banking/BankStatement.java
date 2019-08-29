package awsm.domain.banking;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.temporal.ChronoUnit.MINUTES;

import awsm.domain.offers.DecimalNumber;
import awsm.infra.media.Media;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import org.threeten.extra.LocalDateRange;

class BankStatement {

  private final Collection<TxEntry> entries = new ArrayList<>();

  private final Balance closingBalance;

  private final Balance startingBalance;

  BankStatement(LocalDate from, LocalDate to, Transactions transactions) {
    var startingBalance = transactions.within(LocalDateRange.ofUnboundedStart(from)).sum();

    var closingBalance = transactions
        .within(LocalDateRange.ofClosed(from, to))
        .stream()
        .foldLeft(startingBalance, (balance, tx) -> {
          var runningBalance = tx.apply(balance);
          entries.add(new TxEntry(
              tx.bookingTime(),
              tx.amount().withdrawal(),
              tx.amount().deposit(),
              runningBalance));
          return runningBalance;
        });

    this.startingBalance = new Balance(from, startingBalance);
    this.closingBalance = new Balance(to, closingBalance);
  }

  public void printTo(Media media) {
    media.print("startingBalance", nested -> startingBalance.printTo(nested));
    media.print("closingBalance", nested -> closingBalance.printTo(nested));
    media.print("transactions", entries, (nested, entry) -> entry.printTo(nested));
  }

  static class TxEntry {

    private final LocalDateTime time;

    private final DecimalNumber withdrawal;

    private final DecimalNumber deposit;

    private final DecimalNumber balance;

    TxEntry(LocalDateTime time, DecimalNumber withdrawal, DecimalNumber deposit, DecimalNumber balance) {
      this.time = time.truncatedTo(MINUTES);
      this.withdrawal = withdrawal;
      this.deposit = deposit;
      this.balance = balance;
    }

    private void printTo(Media media) {
      media.print("time", time.format(ISO_LOCAL_DATE_TIME));
      media.print("withdrawal", withdrawal.toString());
      media.print("deposit", deposit.toString());
      media.print("balance", balance.toString());
    }
  }

  static class Balance {

    private final DecimalNumber amount;
    private final LocalDate date;

    Balance(LocalDate date, DecimalNumber amount) {
      this.amount = amount;
      this.date = date;
    }

    private void printTo(Media media) {
      media.print("amount", amount.toString());
      media.print("date", date.format(ISO_DATE));
    }


  }
}
