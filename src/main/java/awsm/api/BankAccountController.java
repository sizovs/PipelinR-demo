package awsm.api;

import static java.lang.String.format;

import awsm.domain.banking.AccountHolder;
import awsm.domain.banking.BankAccount;
import awsm.domain.banking.BankAccountRepository;
import awsm.domain.banking.WithdrawalLimits;
import awsm.domain.core.Amount;
import java.math.BigDecimal;
import lombok.Data;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
class BankAccountController {

  private final BankAccountRepository repo;
  private final Environment env;
  private final Mailer mailer;

  BankAccountController(BankAccountRepository repo, Environment env, Mailer mailer) {
    this.repo = repo;
    this.env = env;
    this.mailer = mailer;
  }

  @PostMapping("/accounts/{iban}/deposits")
  public void deposit(@PathVariable String iban, @RequestParam BigDecimal amount) {
    var account = repo.getOne(iban);
    account.deposit(Amount.of(amount));
  }

  @PostMapping("/accounts/{iban}/withdrawals")
  public void withdraw(@PathVariable String iban, @RequestParam BigDecimal amount) {
    var account = repo.getOne(iban);
    account.withdraw(Amount.of(amount));
  }

  @PostMapping("/accounts")
  public ResponseDto applyForBankAccount(@RequestBody RequestDto request) {
    var accountHolder = new AccountHolder(request.firstName, request.lastName, request.email);
    var withdrawalLimits = new WithdrawalLimits(env);
    var account = new BankAccount(accountHolder, withdrawalLimits);
    account.open();
    account.deposit(openingBonus());
    repo.save(account);
    emailCongratulations(account.holder());
    return new ResponseDto(account.iban(), account.balance() + "");
  }

  private Amount openingBonus() {
    return Amount.of("5.00");
  }

  private void emailCongratulations(AccountHolder holder) {
    var email =
        EmailBuilder.startingBlank()
            .to(holder.email())
            .withSubject("Congratulations!")
            .withPlainText(
                format("Congratulations, %s. Thanks for using our services", holder.name()))
            .buildEmail();
    mailer.sendMail(email);
  }

  @Data
  static class RequestDto {
    public final String firstName;
    public final String lastName;
    public final String personalId;
    public final String email;
  }

  @Data
  static class ResponseDto {
    public final String iban;
    public final String balance;
  }
}
