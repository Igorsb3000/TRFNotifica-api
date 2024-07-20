package ufrn.br.TRFNotifica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ufrn.br.TRFNotifica.model.FailedEmail;

@Repository
public interface FailedEmailRepository extends JpaRepository<FailedEmail, String> {
}