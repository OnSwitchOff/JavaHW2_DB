/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javahw2_db;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import enteties.Groups;
import enteties.Students;
import java.util.Objects;
import jpa.StudentsJpaController;
import jpa.GroupsJpaController;

/**
 *
 * @author OnSwitchOff
 */
public class JavaHW2_DB {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EntityManagerFactory factory = 
            Persistence.createEntityManagerFactory("JavaHW2_DBPU");
        
        for (Groups group : new GroupsJpaController(factory).findGroupsEntities()) {
            System.out.println(group.getGroupName()+":");
            for (Students student : new StudentsJpaController(factory).findStudentsEntities()) {
                if (group.getId() == student.getGroupId().getId()) {
                   System.out.println('\t'+student.getName()); 
                }               
            }            
        }      
    }
}
