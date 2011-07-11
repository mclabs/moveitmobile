package org.openxdata.purcai;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

public class TestData implements Persistent {

	private Vector classes;
	private Vector strms;
	private Vector subjects;
	private Vector papers;
	private Vector testTypes;
	private Vector classStrms;
	private Vector subjectPapers;
	private Vector strmSubjects;
	private Vector classPapers;
	private Vector classTestTypes;


	public Vector getClasses() {
		return classes;
	}

	public void setClasses(Vector classes) {
		this.classes = classes;
	}

	public Vector getClassPapers() {
		return classPapers;
	}

	public void setClassPapers(Vector classPapers) {
		this.classPapers = classPapers;
	}

	public Vector getClassStrms() {
		return classStrms;
	}

	public void setClassStrms(Vector classStrms) {
		this.classStrms = classStrms;
	}

	public Vector getPapers() {
		return papers;
	}

	public void setPapers(Vector papers) {
		this.papers = papers;
	}

	public Vector getStrms() {
		return strms;
	}

	public void setStrms(Vector strms) {
		this.strms = strms;
	}

	public Vector getStrmSubjects() {
		return strmSubjects;
	}

	public void setStrmSubjects(Vector strmSubjects) {
		this.strmSubjects = strmSubjects;
	}

	public Vector getSubjectPapers() {
		return subjectPapers;
	}

	public void setSubjectPapers(Vector subjectPapers) {
		this.subjectPapers = subjectPapers;
	}

	public Vector getSubjects() {
		return subjects;
	}

	public void setSubjects(Vector subjects) {
		this.subjects = subjects;
	}

	public Vector getTestTypes() {
		return testTypes;
	}

	public void setTestTypes(Vector testTypes) {
		this.testTypes = testTypes;
	}

	public Vector getClassTestTypes() {
		return classTestTypes;
	}

	public void setClassTestTypes(Vector classTestTypes) {
		this.classTestTypes = classTestTypes;
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#read(java.io.DataInputStream)
	 */
	public void read(DataInputStream dis) throws IOException, InstantiationException, IllegalAccessException {
		setClasses(PersistentHelper.readBig(dis, new NameValue().getClass()));
		setStrms(PersistentHelper.readBig(dis, new NameValue().getClass()));
		setSubjects(PersistentHelper.readBig(dis, new NameValue().getClass()));
		setPapers(PersistentHelper.readBig(dis, new NameValue().getClass()));
		setTestTypes(PersistentHelper.readBig(dis, new NameValue().getClass()));
		setClassStrms(PersistentHelper.readBig(dis, new KeyValueValue().getClass()));
		setSubjectPapers(PersistentHelper.readBig(dis, new KeyValueValue().getClass()));
		setStrmSubjects(PersistentHelper.readBig(dis, new ValueValue().getClass()));
		setClassPapers(PersistentHelper.readBig(dis, new ValueValue().getClass()));
		setClassTestTypes(PersistentHelper.readBig(dis, new ValueValue().getClass()));
	}

	/**
	 * @see org.fcitmuk.db.util.Persistent#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream dos) throws IOException {
		PersistentHelper.writeBig(getClasses(), dos);
		PersistentHelper.writeBig(getStrms(), dos);
		PersistentHelper.writeBig(getSubjects(), dos);
		PersistentHelper.writeBig(getPapers(), dos);
		PersistentHelper.writeBig(getTestTypes(), dos);
		PersistentHelper.writeBig(getClassStrms(), dos);
		PersistentHelper.writeBig(getSubjectPapers(), dos);
		PersistentHelper.writeBig(getStrmSubjects(), dos);
		PersistentHelper.writeBig(getClassPapers(), dos);
		PersistentHelper.writeBig(getClassTestTypes(), dos);
	}
}
