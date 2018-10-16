/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpa;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import enteties.Groups;
import enteties.Students;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import jpa.exceptions.NonexistentEntityException;

/**
 *
 * @author OnSwitchOff
 */
public class StudentsJpaController implements Serializable {

    public StudentsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Students students) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Groups groupId = students.getGroupId();
            if (groupId != null) {
                groupId = em.getReference(groupId.getClass(), groupId.getId());
                students.setGroupId(groupId);
            }
            em.persist(students);
            if (groupId != null) {
                groupId.getStudentsCollection().add(students);
                groupId = em.merge(groupId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Students students) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Students persistentStudents = em.find(Students.class, students.getId());
            Groups groupIdOld = persistentStudents.getGroupId();
            Groups groupIdNew = students.getGroupId();
            if (groupIdNew != null) {
                groupIdNew = em.getReference(groupIdNew.getClass(), groupIdNew.getId());
                students.setGroupId(groupIdNew);
            }
            students = em.merge(students);
            if (groupIdOld != null && !groupIdOld.equals(groupIdNew)) {
                groupIdOld.getStudentsCollection().remove(students);
                groupIdOld = em.merge(groupIdOld);
            }
            if (groupIdNew != null && !groupIdNew.equals(groupIdOld)) {
                groupIdNew.getStudentsCollection().add(students);
                groupIdNew = em.merge(groupIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = students.getId();
                if (findStudents(id) == null) {
                    throw new NonexistentEntityException("The students with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Students students;
            try {
                students = em.getReference(Students.class, id);
                students.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The students with id " + id + " no longer exists.", enfe);
            }
            Groups groupId = students.getGroupId();
            if (groupId != null) {
                groupId.getStudentsCollection().remove(students);
                groupId = em.merge(groupId);
            }
            em.remove(students);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Students> findStudentsEntities() {
        return findStudentsEntities(true, -1, -1);
    }

    public List<Students> findStudentsEntities(int maxResults, int firstResult) {
        return findStudentsEntities(false, maxResults, firstResult);
    }

    private List<Students> findStudentsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Students.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Students findStudents(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Students.class, id);
        } finally {
            em.close();
        }
    }

    public int getStudentsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Students> rt = cq.from(Students.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
