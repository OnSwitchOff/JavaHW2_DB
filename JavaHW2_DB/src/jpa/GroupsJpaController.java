/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpa;

import enteties.Groups;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import enteties.Students;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import jpa.exceptions.IllegalOrphanException;
import jpa.exceptions.NonexistentEntityException;

/**
 *
 * @author OnSwitchOff
 */
public class GroupsJpaController implements Serializable {

    public GroupsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Groups groups) {
        if (groups.getStudentsCollection() == null) {
            groups.setStudentsCollection(new ArrayList<Students>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Students> attachedStudentsCollection = new ArrayList<Students>();
            for (Students studentsCollectionStudentsToAttach : groups.getStudentsCollection()) {
                studentsCollectionStudentsToAttach = em.getReference(studentsCollectionStudentsToAttach.getClass(), studentsCollectionStudentsToAttach.getId());
                attachedStudentsCollection.add(studentsCollectionStudentsToAttach);
            }
            groups.setStudentsCollection(attachedStudentsCollection);
            em.persist(groups);
            for (Students studentsCollectionStudents : groups.getStudentsCollection()) {
                Groups oldGroupIdOfStudentsCollectionStudents = studentsCollectionStudents.getGroupId();
                studentsCollectionStudents.setGroupId(groups);
                studentsCollectionStudents = em.merge(studentsCollectionStudents);
                if (oldGroupIdOfStudentsCollectionStudents != null) {
                    oldGroupIdOfStudentsCollectionStudents.getStudentsCollection().remove(studentsCollectionStudents);
                    oldGroupIdOfStudentsCollectionStudents = em.merge(oldGroupIdOfStudentsCollectionStudents);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Groups groups) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Groups persistentGroups = em.find(Groups.class, groups.getId());
            Collection<Students> studentsCollectionOld = persistentGroups.getStudentsCollection();
            Collection<Students> studentsCollectionNew = groups.getStudentsCollection();
            List<String> illegalOrphanMessages = null;
            for (Students studentsCollectionOldStudents : studentsCollectionOld) {
                if (!studentsCollectionNew.contains(studentsCollectionOldStudents)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Students " + studentsCollectionOldStudents + " since its groupId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Students> attachedStudentsCollectionNew = new ArrayList<Students>();
            for (Students studentsCollectionNewStudentsToAttach : studentsCollectionNew) {
                studentsCollectionNewStudentsToAttach = em.getReference(studentsCollectionNewStudentsToAttach.getClass(), studentsCollectionNewStudentsToAttach.getId());
                attachedStudentsCollectionNew.add(studentsCollectionNewStudentsToAttach);
            }
            studentsCollectionNew = attachedStudentsCollectionNew;
            groups.setStudentsCollection(studentsCollectionNew);
            groups = em.merge(groups);
            for (Students studentsCollectionNewStudents : studentsCollectionNew) {
                if (!studentsCollectionOld.contains(studentsCollectionNewStudents)) {
                    Groups oldGroupIdOfStudentsCollectionNewStudents = studentsCollectionNewStudents.getGroupId();
                    studentsCollectionNewStudents.setGroupId(groups);
                    studentsCollectionNewStudents = em.merge(studentsCollectionNewStudents);
                    if (oldGroupIdOfStudentsCollectionNewStudents != null && !oldGroupIdOfStudentsCollectionNewStudents.equals(groups)) {
                        oldGroupIdOfStudentsCollectionNewStudents.getStudentsCollection().remove(studentsCollectionNewStudents);
                        oldGroupIdOfStudentsCollectionNewStudents = em.merge(oldGroupIdOfStudentsCollectionNewStudents);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = groups.getId();
                if (findGroups(id) == null) {
                    throw new NonexistentEntityException("The groups with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Groups groups;
            try {
                groups = em.getReference(Groups.class, id);
                groups.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The groups with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Students> studentsCollectionOrphanCheck = groups.getStudentsCollection();
            for (Students studentsCollectionOrphanCheckStudents : studentsCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Groups (" + groups + ") cannot be destroyed since the Students " + studentsCollectionOrphanCheckStudents + " in its studentsCollection field has a non-nullable groupId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(groups);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Groups> findGroupsEntities() {
        return findGroupsEntities(true, -1, -1);
    }

    public List<Groups> findGroupsEntities(int maxResults, int firstResult) {
        return findGroupsEntities(false, maxResults, firstResult);
    }

    private List<Groups> findGroupsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Groups.class));
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

    public Groups findGroups(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Groups.class, id);
        } finally {
            em.close();
        }
    }

    public int getGroupsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Groups> rt = cq.from(Groups.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
