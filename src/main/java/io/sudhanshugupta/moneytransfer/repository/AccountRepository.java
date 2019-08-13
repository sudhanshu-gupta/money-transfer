package io.sudhanshugupta.moneytransfer.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.sudhanshugupta.moneytransfer.entity.Account;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

  @Transactional
  public Optional<Account> getById(long id) {
    return Optional.ofNullable(findById(id));
  }

  @Transactional
  public Optional<Account> findByEmail(String email) {
    Account account = find("SELECT a from Account a where a.email = :em",
        Parameters.with("em", email).map()).firstResult();
    return Optional.ofNullable(account);
  }
}
