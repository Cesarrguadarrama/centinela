package mx.centinela.bootstrap.api;

import mx.centinela.bootstrap.api.dto.AccountSummary;
import mx.centinela.bootstrap.api.dto.PageView;
import mx.centinela.bootstrap.api.dto.TransactionView;
import mx.centinela.bootstrap.infrastructure.queries.AccountQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
class AccountController {

  private final AccountQueryService accounts;

  AccountController(AccountQueryService accounts) {
    this.accounts = accounts;
  }

  @GetMapping("/{clabe}/summary")
  AccountSummary summary(@PathVariable String clabe, @RequestParam(defaultValue = "24") int hours) {
    return accounts.summary(clabe, Math.min(Math.max(hours, 1), 168));
  }

  @GetMapping("/{clabe}/transactions")
  PageView<TransactionView> transactions(
      @PathVariable String clabe,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return accounts.transactions(clabe, page, Math.min(size, 200));
  }
}
