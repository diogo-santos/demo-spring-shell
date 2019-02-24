package com.demo.shell.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.demo.shell.entity.Person;
import com.demo.shell.repo.PersonRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.shell.interactive.enabled=false","spring.shell.script.enabled=false"})
public class PersonCommandTest {
	@Autowired
	private PersonRepository repository;
	@Autowired
	private PersonCommand cmd;

	@Test
	public void addTest() {
		long result = this.cmd.add("firstAdd", "lastAdd");
		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(result);
		assertThat(personList).extracting(Person::getFirstName).contains("firstAdd");
		assertThat(personList).extracting(Person::getSurname).contains("lastAdd");
	}
	
	@Test
	public void editTest() {
		long id = this.repository.save(Person.builder()
										.firstName("test")
										.surname("test").build())
								 .getId();
		this.cmd.edit(id, "firstEdit", "lastEdit");
		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).contains(id);
		assertThat(personList).extracting(Person::getFirstName).contains("firstEdit");
		assertThat(personList).extracting(Person::getSurname).contains("lastEdit");
	}
	
	@Test
	public void deleteTest() {
		long id = this.repository.save(Person.builder()
										.firstName("testDelete")
										.surname("testDelete").build())
								 .getId();
		this.cmd.delete(id);
		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getId).doesNotContain(id);
	}
	
	@Test
	public void countTest() {
		long count = this.cmd.count();
		assertEquals(repository.count() , count);
	}
	
	@Test
	public void listTest() {
		this.repository.save(Person.builder().firstName("firstList1").surname("lastList1").build());
		this.cmd.add("firstList2","lastList2");
		List<Person> personList = this.cmd.list();
		assertThat(personList).extracting(Person::getFirstName).contains("firstList1","firstList2");
		assertThat(personList).extracting(Person::getSurname).contains("lastList1","lastList2");
	}

	@Test
	public void editNotExistTest() {
		String result = this.cmd.edit(-1000000, "test", "test");
		assertNotEquals("Edit success", result);
	}

	@Test
	public void deleteNotExistTest() {
		String result = this.cmd.delete(-1000000);
		assertNotEquals("Delete success", result);
	}

	@Test
	public void addXmlFileTest() throws Exception {
		String xml = "<persons>" +
                         "<person>" +
                            "<firstName>firstXmlFile1</firstName>" +
                            "<surname>lastXmlFile1</surname>" +
                         "</person>" +
                         "<person>" +
                            "<firstName>firstXmlFile2</firstName>" +
                            "<surname>lastXmlFile2</surname>" +
                         "</person>" +
                     "</persons>";
		PersonCommand cmdSpy = Mockito.spy(this.cmd);
		doReturn(new ByteArrayInputStream(xml.getBytes()))
				.when(cmdSpy)
				.newFileInputStream(anyString());

		cmdSpy.addXmlFile("person.xml");

		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getFirstName).contains("firstXmlFile1", "firstXmlFile2");
		assertThat(personList).extracting(Person::getSurname).contains("lastXmlFile1", "lastXmlFile2");
	}

	@Test
	public void addXmlWhenFileNotExistsTest() {
		String result = this.cmd.addXmlFile("");
		assertEquals("File not found", result);
	}

	@Test
	public void addXmlFileWhenXmlBrokenTest() throws Exception {
		String xml = "<persons>" +
						"<person>" +
							"<firstName>firstXmlBroken" +
							"<surname>lastXmlBroken";
		PersonCommand cmdSpy = Mockito.spy(this.cmd);
		doReturn(new ByteArrayInputStream(xml.getBytes()))
				.when(cmdSpy)
				.newFileInputStream(anyString());

		String result = cmdSpy.addXmlFile("person.xml");

		assertEquals("Error processing xml file", result);
	}

	@Test
	public void addXmlTextTest() {
		String xml = "<persons>" +
                         "<person>" +
                            "<firstName>firstXml1</firstName>" +
                            "<surname>lastXml1</surname>" +
                         "</person>" +
						 "<person>" +
							"<firstName>firstXml2</firstName>" +
							"<surname>lastXml2</surname>" +
						 "</person>" +
                    "</persons>";
		this.cmd.addXmlText(xml);

		List<Person> personList = this.repository.findAll();
		assertThat(personList).extracting(Person::getFirstName).contains("firstXml1", "firstXml2");
		assertThat(personList).extracting(Person::getSurname).contains("lastXml1", "lastXml2");
	}

	@Test
	public void addXmlTextWhenXmlBrokenTest() {
		String xml = "<person>" +
						"<firstName>firstXml" +
						"<surname>lastXml";
		String result = this.cmd.addXmlText(xml);
		assertEquals("Error processing xml", result);
	}
}