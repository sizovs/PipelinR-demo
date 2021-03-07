package awsm.api;

import awsm.domain.banking.AccountHolder;
import awsm.domain.banking.BankAccount;
import awsm.domain.banking.BankAccountRepository;
import awsm.domain.banking.WithdrawalLimits;
import awsm.domain.core.Amount;
import lombok.Data;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@RestController
class BankAccountController {

  private final BankAccountRepository repo;
  private final WithdrawalLimits withdrawalLimits;
  private final Mailer mailer;

  BankAccountController(BankAccountRepository repo, WithdrawalLimits withdrawalLimits, Mailer mailer) {
    this.repo = repo;
    this.withdrawalLimits = withdrawalLimits;
    this.mailer = mailer;
  }

  @PostMapping("/bank-accounts")
  ResponseDto applyForBankAccount(@RequestBody RequestDto request) {
    var accountHolder = new AccountHolder(request.firstName, request.lastName, request.email);
    var account = new BankAccount(accountHolder, withdrawalLimits);
    account.open();
    account.deposit(openingBonus());
    repo.save(account);
    emailCongratulations(account);
    return new ResponseDto(account.iban());
  }

  private Amount openingBonus() {
    return Amount.of("5.00");
  }

  private void emailCongratulations(BankAccount account) {
    var email =
        EmailBuilder.startingBlank()
            .to(account.holder().email())
            .withSubject("Congratulations!")
            .withPlainText(
                format(
                    "Congratulations, %s. Thanks for using our services",
                    account.holder().name()))
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
  }
}
