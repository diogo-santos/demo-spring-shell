package com.demo.shell.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.demo.shell.entity.Person;
import com.demo.shell.repo.PersonRepository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

@ShellComponent
public class PersonCommand {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PersonCommand.class);

	private final PersonRepository repository;
	
	public PersonCommand(PersonRepository repository) {
		super();
		this.repository = repository;
	}
	
    @ShellMethod("Add a person [firstName, surname] returns id")
    long add(String firstName, String surname) {
        logger.debug("In add with firstName={} surname={}", firstName, surname);
    	Person personSaved = this.repository.save(Person.builder()
													.firstName(firstName)
													.surname(surname)
													.build());
        long id = personSaved.getId();
        logger.debug("Out add with {} ", id);
    	return id;
    }
    
    @ShellMethod("Edit a person [id, firstName, surname]")
    String edit(long id, String firstName, String surname) {
        logger.debug("In edit with id={} firstName={} surname={}", id, firstName, surname);
    	String message = String.format("Person id %s does not exist", id);
		Optional<Person> personOpt = this.repository.findById(id);
        if (personOpt.isPresent()) {
        	Person person = personOpt.get();
        	person.setFirstName(firstName);
			person.setSurname(surname);
            this.repository.save(person);
			message = "Edit success";
		}
        logger.debug("Out edit with {} ", message);
    	return message;
    }
    
    @ShellMethod("Delete a person [id]")
    String delete(long id) {
        logger.debug("In delete with {} ", id);
    	String message = "Delete success";
        try {
            this.repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
			message = String.format("Person id %s does not exist", id);
		}
        logger.debug("Out delete with {} ", message);
    	return message;
    }
    
    @ShellMethod("Count Number of Persons")
    long count() {
        long count = this.repository.count();
        logger.debug("Out count with {} ", count);
        return count;
    }
    
    @ShellMethod("List Persons")
    List<Person> list() {
	    List<Person> list = this.repository.findAll();
        logger.debug("Out list with {} ", list.size());
        return list;
    }

    @ShellMethod("Add a person from xml file [xmlFilePath] - windows path eg. c:/dir/file.xml, xml content eg. <person><firstName></firstName><surname></surname></person>")
    long addXmlFile(String xmlFilePath) {
        logger.debug("In addXmlFile with {} ", xmlFilePath);
        long result = 0;
        try {
            Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(Person.class).createUnmarshaller();
            Person person = (Person) jaxbUnmarshaller.unmarshal(this.newFileInputStream(xmlFilePath));
            Person personSaved = this.repository.save(person);
            result = personSaved.getId();
        } catch (Exception e) {
            logger.debug("Error addXmlFile with {} ", xmlFilePath);
            logger.error(e.getMessage(), e);
        }
        logger.debug("Out addXmlFile with {} ", result);
        return result;
    }

    InputStream newFileInputStream(String path) throws Exception {
	    return new FileInputStream(new File(path));
    }

    @ShellMethod("Add a person from xml text [xmlText] - eg. <person><firstName>name</firstName><surname>surname</surname></person>")
    long addXmlText(String xmlText) {
        logger.debug("In addXmlText with {} ", xmlText);
        long result = 0;
        try {
            Unmarshaller jaxbUnmarshaller = JAXBContext.newInstance(Person.class).createUnmarshaller();
            Person person = (Person) jaxbUnmarshaller.unmarshal(new StringReader(xmlText));
            Person personSaved = this.repository.save(person);
            result = personSaved.getId();
        } catch (Exception e) {
            logger.debug("Error addXmlText with {} ", xmlText);
            logger.error(e.getMessage(), e);
        }
        logger.debug("Out addXmlText with {} ", result);
        return result;
    }
}
