
package edu.cmu.andrew.kmakaew;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.6-1b01 
 * Generated source version: 2.2
 * 
 */
@WebService(name = "StudentListService", targetNamespace = "http://kmakaew.andrew.cmu.edu/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface StudentListService {


    /**
     * 
     * @param arg0
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "deleteStudent", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.DeleteStudent")
    @ResponseWrapper(localName = "deleteStudentResponse", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.DeleteStudentResponse")
    @Action(input = "http://kmakaew.andrew.cmu.edu/StudentListService/deleteStudentRequest", output = "http://kmakaew.andrew.cmu.edu/StudentListService/deleteStudentResponse")
    public boolean deleteStudent(
        @WebParam(name = "arg0", targetNamespace = "")
        Short arg0);

    /**
     * 
     * @param arg0
     * @return
     *     returns edu.cmu.andrew.kmakaew.Studenttable
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "readStudent", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.ReadStudent")
    @ResponseWrapper(localName = "readStudentResponse", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.ReadStudentResponse")
    @Action(input = "http://kmakaew.andrew.cmu.edu/StudentListService/readStudentRequest", output = "http://kmakaew.andrew.cmu.edu/StudentListService/readStudentResponse")
    public Studenttable readStudent(
        @WebParam(name = "arg0", targetNamespace = "")
        Short arg0);

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "createStudent", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.CreateStudent")
    @ResponseWrapper(localName = "createStudentResponse", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.CreateStudentResponse")
    @Action(input = "http://kmakaew.andrew.cmu.edu/StudentListService/createStudentRequest", output = "http://kmakaew.andrew.cmu.edu/StudentListService/createStudentResponse")
    public boolean createStudent(
        @WebParam(name = "arg0", targetNamespace = "")
        Short arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        Double arg2);

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "updateStudent", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.UpdateStudent")
    @ResponseWrapper(localName = "updateStudentResponse", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.UpdateStudentResponse")
    @Action(input = "http://kmakaew.andrew.cmu.edu/StudentListService/updateStudentRequest", output = "http://kmakaew.andrew.cmu.edu/StudentListService/updateStudentResponse")
    public boolean updateStudent(
        @WebParam(name = "arg0", targetNamespace = "")
        Short arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        Double arg2);

    /**
     * 
     * @return
     *     returns java.util.List<java.lang.Object>
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "findAllStudentNames", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.FindAllStudentNames")
    @ResponseWrapper(localName = "findAllStudentNamesResponse", targetNamespace = "http://kmakaew.andrew.cmu.edu/", className = "edu.cmu.andrew.kmakaew.FindAllStudentNamesResponse")
    @Action(input = "http://kmakaew.andrew.cmu.edu/StudentListService/findAllStudentNamesRequest", output = "http://kmakaew.andrew.cmu.edu/StudentListService/findAllStudentNamesResponse")
    public List<Object> findAllStudentNames();

}
