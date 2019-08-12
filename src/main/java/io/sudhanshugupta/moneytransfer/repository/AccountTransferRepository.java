package io.sudhanshugupta.moneytransfer.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.sudhanshugupta.moneytransfer.entity.AccountTransfer;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountTransferRepository implements PanacheRepository<AccountTransfer> {

}
