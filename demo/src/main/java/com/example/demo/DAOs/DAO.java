package com.example.demo.DAOs;

import java.util.List;

public interface DAO<T>{
        T getById(Integer id);

        T saveOrUpdate(T domainObject);

        void delete(Integer id);
}
