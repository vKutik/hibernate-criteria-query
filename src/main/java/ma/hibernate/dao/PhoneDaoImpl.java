package ma.hibernate.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't create new phone : " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        StringBuilder stringBuilderInfoMap = new StringBuilder();
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = criteriaBuilder.createQuery(Phone.class);
            Root<Phone> root = query.from(Phone.class);
            List<Predicate> predicates = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String nameField = entry.getKey();
                String[] parameters = entry.getValue();
                stringBuilderInfoMap
                        .append("key: ")
                        .append(nameField)
                        .append(", value [ ");
                In<String> inPredicate = criteriaBuilder.in(root.get(nameField));
                for (String parameter : parameters) {
                    stringBuilderInfoMap
                            .append(parameter)
                            .append(", ");
                    inPredicate.value(parameter);
                }
                predicates.add(inPredicate);
                stringBuilderInfoMap.append("], ").append("\n");
            }
            Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
            Predicate finallyPredicate = criteriaBuilder.and(predicatesArray);
            query.where(finallyPredicate);
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            throw new RuntimeException(
                    "can't get all phones with parameters : " + stringBuilderInfoMap, e);
        }
    }
}
