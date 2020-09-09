package com.example.demo.DAOs;

import com.example.demo.Entities.User;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
public class UserDAOImpl extends AbstractDAO implements UserDAO {

    @Override
    public User getById(Integer id) {
        EntityManager em = emf.createEntityManager();
        return em.find(User.class, id);
    }

    @Override
    public User saveOrUpdate(User user) {
        //begin transaction
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User savedUser = em.merge(user);
        em.getTransaction().commit();
        return savedUser;
    }

    @Override
    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.find(User.class, id));
        em.getTransaction().commit();
    }

    public User findByUsername(String username){
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("SELECT u FROM User u WHERE username = ?1");
        query.setParameter(1, username);
        List<User> userList = (List<User>) query.getResultList();
        User foundUser;
        if(userList.size() >= 1) foundUser = userList.get(0);
        else foundUser = null;
        return foundUser;
    }




}
