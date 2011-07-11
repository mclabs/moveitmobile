package org.openxdata.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.openxdata.db.util.Persistent;
import org.openxdata.db.util.PersistentHelper;

/**
 * Represents data collected for a question.
 * In the MCV world, this is the model representing the question data.
 * 
 * @author Daniel Kayiwa
 *
 */
public class QuestionData implements Persistent {

        public static final String TRUE_VALUE = "true";
        public static final String FALSE_VALUE = "false";
        public static final String TRUE_DISPLAY_VALUE = "Yes";
        public static final String FALSE_DISPLAY_VALUE = "No";
        public static final String NO_SELECTION_VALUE = "No Selection";
        public static final String MULITPLE_SELECT_VALUE_SEPARATOR = " ";
        public static final String MULITPLE_SELECT_TEXT_SEPARATOR = ",";
        public static final String REPEAT_VALUE_SEPARATOR = " ";
        public static final String REPEAT_TEXT_SEPARATOR = "|";
        public static byte dateDisplayFormat = 0;
        public static byte dateSubmitFormat = 2;
        /** The answer of the question. */
        private Object answer;
        /** For Single Select, this is a zero based index of the selected option answer. This is just for increased performance.
         * For Multiple Select, this is a list of indices for selected answers. This is for increased performance,
         * and is particularly useful for questions that expect multiple answers
         * to be picked from a list.
         */
        private Object optionAnswerIndices;
        /** Reference to the question definition for this data. */
        private QuestionDef def;
        /** The numeric unique identifier for the question that this data is collected for. */
        private byte id = OpenXdataConstants.NULL_ID;
        private String dataDescription;

        /** Construct a new question data object. */
        public QuestionData() {
                super();
        }

        /** Copy constructor. */
        public QuestionData(QuestionData data) {
                setId(data.getId());
                setDef(data.getDef());
                copyAnswersAndIndices(data); //TODO Need to copy repeats too.
        }

        /**
         * Constructs a new question data object from a definition.
         *
         * @param def - reference to the question definition.
         */
        public QuestionData(QuestionDef def) {
                this();
                setDef(def);
        }

        public byte getId() {
                return id;
        }

        public void setId(byte id) {
                this.id = id;
        }

        public Object getAnswer() {
                return answer;
        }

        public String getDataDescription() {
                return dataDescription;
        }

        public void setDataDescription(String dataDescription) {
                this.dataDescription = dataDescription;
        }

        public String getListExclusiveAnswer() {
                if (answer == null) {
                        return null;
                }
                return ((OptionData) answer).getValue();
        }

        public String getListMultileAnswers() {
                if (answer == null) {
                        return null;
                }

                Vector list = (Vector) answer;
                String s = null;
                for (byte i = 0; i < list.size(); i++) {
                        if (s != null) {
                                s += ",";
                        } else {
                                s = "";
                        }
                        s += ((OptionData) list.elementAt(i)).getValue();
                }

                return s;
        }

        public boolean isDateFunction(Object value) {
                if (value == null) {
                        return false;
                }

                return (value.equals("'now()'") || value.equals("'date()'")
                        || value.equals("'getdate()'") || value.equals("'today()'"));
        }

        public boolean isDateTime() {
                return (getDef() != null && (getDef().getType() == QuestionDef.QTN_TYPE_DATE_TIME
                        || getDef().getType() == QuestionDef.QTN_TYPE_DATE
                        || getDef().getType() == QuestionDef.QTN_TYPE_TIME));
        }

        public void setAnswer(Object answer) {
                if (isDateTime() && isDateFunction(answer)) {
                        this.answer = new Date();
                } else {
                        this.answer = answer;
                }
        }

        public boolean setOptionValueIfOne() {
                if (!(getDef().getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || getDef().getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC)) {
                        return false;
                }

                Vector options = getDef().getOptions();
                if (options != null && options.size() == 1) {
                        setOptionAnswer(((OptionDef) options.elementAt(0)).getVariableName());
                        return true;
                }
                return false;
        }

        public void setTextAnswer(String textAnswer) {
                switch (getDef().getType()) {
                        case QuestionDef.QTN_TYPE_BOOLEAN:
                                answer = fromString2Boolean(textAnswer);
                                break;
                        case QuestionDef.QTN_TYPE_DATE:
                        case QuestionDef.QTN_TYPE_DATE_TIME:
                        case QuestionDef.QTN_TYPE_TIME:
                                answer = textAnswer;
                                break;
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
                                setOptionAnswer(textAnswer);
                                break;
                        case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
                                setOptionAnswers(split(textAnswer, OptionDef.SEPARATOR_CHAR));
                                break;
                        case QuestionDef.QTN_TYPE_DECIMAL:
                        case QuestionDef.QTN_TYPE_NUMERIC:
                        case QuestionDef.QTN_TYPE_TEXT:
                                answer = textAnswer;
                                break;
                }
        }

        public void setOptionAnswer(String textAnswer) {
                for (byte i = 0; i < getDef().getOptions().size(); i++) {
                        OptionDef optionDef = (OptionDef) getDef().getOptions().elementAt(i);
                        //if(optionDef.getVariableName().equals(textAnswer)){
                        if (optionDef.getVariableName().equals(textAnswer) ||
                                optionDef.getText().equals(textAnswer)) {
                                setAnswer(new OptionData(optionDef));
                                setOptionAnswerIndices(new Byte(i));
                                break;
                        }
                }
        }

        public void setOptionAnswers(Vector vals) {
                Vector optionAnswers = new Vector();
                Vector optionAnswerIndices = new Vector();
                for (byte j = 0; j < vals.size(); j++) {
                        String strVal = (String) vals.elementAt(j);
                        for (byte i = 0; i < getDef().getOptions().size(); i++) {
                                OptionDef option = (OptionDef) getDef().getOptions().elementAt(i);
                                if (option.getVariableName().equals(strVal)) {
                                        optionAnswers.addElement(new OptionData(option));
                                        optionAnswerIndices.addElement(new Byte(i));
                                        break;
                                }
                        }
                }

                setAnswer(optionAnswers);
                setOptionAnswerIndices(optionAnswerIndices);
        }

        public Vector split(String contents, char separator) {
                Vector ret = new Vector();

                int j = 0, i = contents.indexOf(separator, j);
                if (i != -1) {
                        while (i > -1) {
                                ret.addElement(contents.substring(j, i));
                                j = i + 1;
                                i = contents.indexOf(separator, j);
                        }
                        if (j > 0) {
                                ret.addElement(contents.substring(j, contents.length()));
                        }
                } else {
                        ret.addElement(contents); //one value found
                }
                return ret;
        }

        public Object getOptionAnswerIndices() {
                return optionAnswerIndices;
        }

        public void setOptionAnswerIndices(Object optionAnswerIndices) {
                this.optionAnswerIndices = optionAnswerIndices;
        }

        public QuestionDef getDef() {
                return def;
        }

        public void setDef(QuestionDef def) {
                this.def = def;
                setId(def.getId());
                if (def.getDefaultValue() != null && getAnswer() == null) {
                        setTextAnswer(def.getDefaultValue());
                }
        }

        private void copyAnswersAndIndices(QuestionData data) {
                if (data.getAnswer() != null) {
                        if (getDef().getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE || getDef().getType() == QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC) {
                                setAnswer(new OptionData((OptionData) data.getAnswer()));
                                setOptionAnswerIndices(data.getOptionAnswerIndices());
                        } else if (getDef().getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) {
                                Vector tempAnswer = new Vector();
                                Vector ansrs = (Vector) data.getAnswer();
                                for (int i = 0; i < ansrs.size(); i++) {
                                        tempAnswer.addElement(new OptionData((OptionData) ansrs.elementAt(i)));
                                }
                                setAnswer(tempAnswer);

                                tempAnswer = new Vector();
                                ansrs = (Vector) data.getOptionAnswerIndices();
                                for (int i = 0; i < ansrs.size(); i++) {
                                        tempAnswer.addElement(ansrs.elementAt(i));
                                }
                                setOptionAnswerIndices(tempAnswer);
                        } else if (getDef().getType() == QuestionDef.QTN_TYPE_IMAGE
                                || getDef().getType() == QuestionDef.QTN_TYPE_AUDIO
                                || getDef().getType() == QuestionDef.QTN_TYPE_VIDEO) {

                                byte[] srcAnswer = (byte[]) data.getAnswer();
                                int len = srcAnswer.length;
                                byte[] dstAnswer = new byte[len];
                                for (int index = 0; index < len; index++) {
                                        dstAnswer[index] = srcAnswer[index];
                                }
                                setAnswer(dstAnswer);
                        } else {
                                setAnswer(data.getAnswer());
                        }
                }
        }

        /**
         * Check to see if an answer is supplied for a question.
         *
         * @return - true when answered, else false.
         */
        public boolean isAnswered() {
                switch (getDef().getType()) {
                        case QuestionDef.QTN_TYPE_BOOLEAN:
                        case QuestionDef.QTN_TYPE_DATE:
                        case QuestionDef.QTN_TYPE_DATE_TIME:
                        case QuestionDef.QTN_TYPE_TIME:
                        case QuestionDef.QTN_TYPE_IMAGE:
                        case QuestionDef.QTN_TYPE_VIDEO:
                        case QuestionDef.QTN_TYPE_AUDIO:
                        case QuestionDef.QTN_TYPE_GPS:
                                return getAnswer() != null;
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
                                return getAnswer() != null;
                        case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
                                return getAnswer() != null && ((Vector) getAnswer()).size() > 0;
                        case QuestionDef.QTN_TYPE_DECIMAL:
                        case QuestionDef.QTN_TYPE_NUMERIC:
                        case QuestionDef.QTN_TYPE_TEXT:
                                return getAnswer() != null && this.getAnswer().toString().length() > 0;
                }
                //TODO need to handle other user defined types.
                return false;
        }

        /**
         * Check whether a question's data is entered correctly.
         * No missing mandatory fields, not values out of range, etc.
         *
         * @return - true if the data is correct, else false.
         */
        public boolean isValid() {
                if (this.getDef().isMandatory() && !this.isAnswered()) {
                        return false;
                }
                return true;
        }

        //TODO This does not belong here.
        public static String DateToString(Date d, byte format) {
                Calendar cd = Calendar.getInstance(OpenXdataConstants.DEFAULT_TIME_ZONE);
                cd.setTime(d);
                String year = "" + cd.get(Calendar.YEAR);
                String month = "" + (cd.get(Calendar.MONTH) + 1);
                String day = "" + cd.get(Calendar.DAY_OF_MONTH);

                if (month.length() < 2) {
                        month = "0" + month;
                }

                if (day.length() < 2) {
                        day = "0" + day;
                }

                if (format == 0) {
                        return day + "-" + month + "-" + year;
                } else if (format == 1) {
                        return month + "-" + day + "-" + year;
                }
                //return day + "-" + month + "-" + year;
                //TODO The date format should be flexibly set by the user.
                return year + "-" + month + "-" + day;
        }

        public static String TimeToString(Date d) {
                Calendar cd = Calendar.getInstance(OpenXdataConstants.DEFAULT_TIME_ZONE);
                cd.setTime(d);
                String hour = "" + cd.get(Calendar.HOUR);
                String minute = "" + (cd.get(Calendar.MINUTE));
                String second = "" + (cd.get(Calendar.SECOND));

                if (hour.length() < 2) {
                        hour = "0" + hour;
                }

                if (minute.length() < 2) {
                        minute = "0" + minute;
                }

                if (second.length() < 2) {
                        second = "0" + second;
                }

                //return day + "-" + month + "-" + year;
                //TODO The time format should be flexibly set by the user.
                return hour + ":" + minute + ":" + second + " " + ((cd.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM");
        }

        public static String DateTimeToString(Date d, byte format) {
                return DateToString(d, format) + " " + TimeToString(d);
        }

        /**
         * Gets the answer of a question in string format regardless of the question type.
         *
         * @return - the string value of the answer.
         */
        public String getTextAnswer() {

                String val = null;

                try {
                        if (getAnswer() != null) {
                                switch (getDef().getType()) {
                                        case QuestionDef.QTN_TYPE_BOOLEAN:
                                                val = fromBoolean2DisplayString(getAnswer());
                                                break;
                                        case QuestionDef.QTN_TYPE_DECIMAL:
                                        case QuestionDef.QTN_TYPE_NUMERIC:
                                        case QuestionDef.QTN_TYPE_TEXT:
                                                val = getAnswer().toString();
                                                break;
                                        case QuestionDef.QTN_TYPE_DATE:
                                                val = DateToString((Date) getAnswer(), dateDisplayFormat);
                                                break;
                                        case QuestionDef.QTN_TYPE_DATE_TIME:
                                                val = DateTimeToString((Date) getAnswer(), dateDisplayFormat);
                                                break;
                                        case QuestionDef.QTN_TYPE_TIME: {
                                                val = TimeToString((Date) getAnswer());
                                                break;
                                        }
                                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
                                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
                                                val = ((OptionData) getAnswer()).toString();
                                                break;
                                        case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
                                                String s = "";
                                                Vector optionAnswers = (Vector) getAnswer();
                                                for (byte i = 0; i < optionAnswers.size(); i++) {
                                                        if (s.length() != 0) {
                                                                s += MULITPLE_SELECT_TEXT_SEPARATOR;
                                                        }
                                                        s += ((OptionData) optionAnswers.elementAt(i)).getDef().getText();
                                                }
                                                val = s;
                                                break;
                                        case QuestionDef.QTN_TYPE_REPEAT:
                                                s = "";
                                                RepeatQtnsDataList list = (RepeatQtnsDataList) getAnswer();
                                                for (byte i = 0; i < list.size(); i++) {
                                                        if (s.length() != 0) {
                                                                s += REPEAT_TEXT_SEPARATOR;
                                                        }
                                                        s += list.getRepeatQtnsData(i).toString();
                                                }
                                                val = s;
                                                break;
                                        case QuestionDef.QTN_TYPE_IMAGE:
                                        case QuestionDef.QTN_TYPE_VIDEO:
                                        case QuestionDef.QTN_TYPE_AUDIO:
                                                if (answer != null) {
                                                        int size = ((byte[]) answer).length;
                                                        if (getDef().getType() == QuestionDef.QTN_TYPE_VIDEO) {
                                                                val = size + "=VIDEO"; //+size+ " bytes";
                                                        } else if (getDef().getType() == QuestionDef.QTN_TYPE_AUDIO) {
                                                                val = size + "=AUDIO"; //+size+ " bytes";
                                                        } else {
                                                                val = size + "=PICTURE"; //+size+ " bytes";
                                                        }
                                                } else {
                                                        val = "NULL";
                                                }
                                                break;
                                        case QuestionDef.QTN_TYPE_GPS:
                                                String ans = (String) answer;
                                                int pos1 = ans.indexOf(',');
                                                val = ans.substring(0, pos1);

                                                int pos2 = ans.lastIndexOf(',');
                                                val += "," + ans.substring(pos1 + 1, pos2);

                                                val += "," + ans.substring(pos2 + 1);
                                                break;
                                        default:
                                                val = "Not Implemented yet.";
                                }
                        }
                } catch (Exception ex) {
                        if (getAnswer() != null) {
                                val = getAnswer().toString();
                        } else {
                                val = "ERROR";
                        }
                }

                return val;
        }

        public static String fromBoolean2DisplayString(Object boolVal) {
                if (((Boolean) boolVal).booleanValue()) {
                        return TRUE_DISPLAY_VALUE;
                }
                return FALSE_DISPLAY_VALUE;
        }

        public static String fromBoolean2ValueString(Object boolVal) {
                if (((Boolean) boolVal).booleanValue()) {
                        return TRUE_VALUE;
                }
                return FALSE_VALUE;
        }

        public static Boolean fromString2Boolean(String val) {
                if (val.equals(TRUE_VALUE)) {
                        return new Boolean(true);
                }
                return new Boolean(false);
        }

        //TODO This method needs to be refactored with the getTextAnswer()
        /**
         * Gets the answer of a question in string format regardless of the question type.
         * The difference with this method and the getTextAnswer() is that this returns
         * the underlying values instead of text for the select from list question types.
         *
         * @return - the string value of the answer.
         */
        public String getValueAnswer() {

                String val = null;

                try {
                        if (getAnswer() != null) {
                                switch (getDef().getType()) {
                                        case QuestionDef.QTN_TYPE_BOOLEAN:
                                                val = fromBoolean2ValueString(getAnswer());
                                                break;
                                        case QuestionDef.QTN_TYPE_DECIMAL:
                                        case QuestionDef.QTN_TYPE_NUMERIC:
                                        case QuestionDef.QTN_TYPE_TEXT:
                                        case QuestionDef.QTN_TYPE_GPS:
                                                val = getAnswer().toString();
                                                break;
                                        case QuestionDef.QTN_TYPE_DATE:
                                                val = DateToString((Date) getAnswer(), dateSubmitFormat);
                                                break;
                                        case QuestionDef.QTN_TYPE_DATE_TIME:
                                                val = DateTimeToString((Date) getAnswer(), dateSubmitFormat);
                                                break;
                                        case QuestionDef.QTN_TYPE_TIME:
                                                val = TimeToString((Date) getAnswer());
                                                break;
                                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
                                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
                                                val = ((OptionData) getAnswer()).getValue();
                                                break;
                                        case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
                                                String s = "";
                                                Vector optionAnswers = (Vector) getAnswer();
                                                for (byte i = 0; i < optionAnswers.size(); i++) {
                                                        if (s.length() != 0) {
                                                                s += MULITPLE_SELECT_VALUE_SEPARATOR;
                                                        }
                                                        s += ((OptionData) optionAnswers.elementAt(i)).getValue();
                                                }
                                                val = s;
                                                break;
                                        case QuestionDef.QTN_TYPE_REPEAT:
                                                /*s = ""; RepeatQtnsDataList list = (RepeatQtnsDataList)getAnswer();
                                                for(byte i=0; i<list.size(); i++){
                                                if(s.length() != 0)
                                                s += REPEAT_VALUE_SEPARATOR;
                                                s += list.getRepeatQtnsData(i).toString();
                                                }
                                                val = s;*/
                                                val = ((RepeatQtnsDataList) getAnswer()).size() + "";
                                                break;
                                        case QuestionDef.QTN_TYPE_IMAGE:
                                        case QuestionDef.QTN_TYPE_VIDEO:
                                        case QuestionDef.QTN_TYPE_AUDIO:
                                                if (answer != null) {
                                                        if (getDef().getType() == QuestionDef.QTN_TYPE_VIDEO) {
                                                                val = "VIDEO";
                                                        } else if (getDef().getType() == QuestionDef.QTN_TYPE_AUDIO) {
                                                                val = "AUDIO";
                                                        } else {
                                                                val = "PICTURE";
                                                        }
                                                } else {
                                                        val = null; //"NULL";
                                                }
                                                break;
                                        default:
                                                val = "Not Implemented yet.";
                                }
                        } else if (getDef().getType() == QuestionDef.QTN_TYPE_REPEAT) {
                                val = "0";
                        }
                } catch (Exception ex) {
                        if (getAnswer() != null) {
                                val = getAnswer().toString();
                        } else {
                                val = "ERROR";
                        }
                }

                return val;
        }

        /**
         * Checks if a value is one of the multiple selected values answered.
         *
         * @param val the value to check
         * @return true if so, else false
         */
        public boolean answerContainsValue(String val) {
                if (getAnswer() == null) {
                        return false;
                }

                if (getDef().getType() == QuestionDef.QTN_TYPE_LIST_MULTIPLE) {
                        Vector optionAnswers = (Vector) getAnswer();
                        for (byte i = 0; i < optionAnswers.size(); i++) {
                                if (((OptionData) optionAnswers.elementAt(i)).getValue().equals(val)) {
                                        return true;
                                }
                        }
                        return false;
                }

                return val.equals(getValueAnswer());
        }

        public String toString() {
//		TODO This method should be refactored with the one above.
                String val = getDef().getText();

                if (dataDescription != null && dataDescription.trim().length() > 0) {
                        val = dataDescription;
                }

                if (getTextAnswer() != null && getTextAnswer().length() > 0) {
                        val += " {" + getTextAnswer() + "}";
                }

                return val;
        }

        public String getText() {
                String val = getDef().getText();

                if (dataDescription != null && dataDescription.trim().length() > 0) {
                        val = dataDescription;
                }

                return val;
        }

        /**
         * Reads the question data object from the supplied stream.
         *
         * @param dis - the stream to read from.
         * @throws IOException
         * @throws InstantiationException
         * @throws IllegalAccessException
         */
        public void read(DataInputStream dis) throws IOException, IllegalAccessException, InstantiationException {
                setId(dis.readByte());
                readAnswer(dis, dis.readByte());
        }

        /**
         * Reads an answer from the stream.
         *
         * @param dis
         * @param type
         * @throws IOException
         * @throws IllegalAccessException
         * @throws InstantiationException
         */
        public void readAnswer(DataInputStream dis, byte type) throws IOException, IllegalAccessException, InstantiationException {
                switch (type) {
                        case QuestionDef.QTN_TYPE_BOOLEAN:
                                setAnswer(PersistentHelper.readBoolean(dis));
                                break;
                        case QuestionDef.QTN_TYPE_TEXT:
                        case QuestionDef.QTN_TYPE_DECIMAL:
                        case QuestionDef.QTN_TYPE_NUMERIC:
                        case QuestionDef.QTN_TYPE_GPS:
                                setAnswer(PersistentHelper.readUTF(dis));
                                break;
                        case QuestionDef.QTN_TYPE_DATE:
                        case QuestionDef.QTN_TYPE_DATE_TIME:
                        case QuestionDef.QTN_TYPE_TIME:
                                setAnswer(PersistentHelper.readDate(dis));
                                break;
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
                                if (dis.readBoolean()) {
                                        OptionData option = new OptionData();
                                        option.read(dis);
                                        setAnswer(option);

                                        setOptionAnswerIndices(new Byte(dis.readByte()));
                                }
                                break;
                        case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
                                if (dis.readBoolean()) {
                                        setAnswer(PersistentHelper.read(dis, new OptionData().getClass()));

                                        byte count = dis.readByte(); //should always be greater than zero
                                        Vector col = new Vector();
                                        for (byte i = 0; i < count; i++) {
                                                col.addElement(new Byte(dis.readByte()));
                                        }
                                        setOptionAnswerIndices(col);
                                }
                                break;
                        case QuestionDef.QTN_TYPE_REPEAT:
                                if (dis.readBoolean()) {
                                        RepeatQtnsDataList repeatQtnsDataList = new RepeatQtnsDataList();
                                        repeatQtnsDataList.read(dis);
                                        setAnswer(repeatQtnsDataList);
                                }
                                break;
                        case QuestionDef.QTN_TYPE_IMAGE:
                        case QuestionDef.QTN_TYPE_VIDEO:
                        case QuestionDef.QTN_TYPE_AUDIO:
                                if (dis.readBoolean()) {
                                        int size = dis.readInt();
                                        byte[] data = new byte[size];

                                        //The line below results into wiered bugs and therefore
                                        //am commenting it out and replacing it with reading data byte per byte.

                                        //dis.read(data);

                                        for (int index = 0; index < size; index++) {
                                                data[index] = dis.readByte();
                                        }

                                        answer = data;
                                }
                                break;
                }
        }

        /**
         * Writes the question data object to the supplied stream.
         *
         * @param dos - the stream to write to.
         * @throws IOException
         */
        public void write(DataOutputStream dos) throws IOException {
                dos.writeByte(getId());

                //This type is only used when reading data back from storage.
                //Otherwise it is not kept in memory because it can be got from the QuestionDef.
                dos.writeByte(getDef().getType());
                writeAnswer(dos);
        }

        private void writeAnswer(DataOutputStream dos) throws IOException {
                switch (getDef().getType()) {
                        case QuestionDef.QTN_TYPE_BOOLEAN:
                                PersistentHelper.writeBoolean(dos, (Boolean) getAnswer());
                                break;
                        case QuestionDef.QTN_TYPE_TEXT:
                        case QuestionDef.QTN_TYPE_DECIMAL:
                        case QuestionDef.QTN_TYPE_NUMERIC:
                        case QuestionDef.QTN_TYPE_GPS:
                                PersistentHelper.writeUTF(dos, getTextAnswer());
                                break;
                        case QuestionDef.QTN_TYPE_DATE:
                        case QuestionDef.QTN_TYPE_DATE_TIME:
                        case QuestionDef.QTN_TYPE_TIME:
                                Date d = null;
                                if (getAnswer() instanceof Date) //TODO Do this for all others
                                {
                                        d = (Date) getAnswer();
                                }
                                PersistentHelper.writeDate(dos, d);
                                break;
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE:
                        case QuestionDef.QTN_TYPE_LIST_EXCLUSIVE_DYNAMIC:
                                if (getAnswer() != null) {
                                        dos.writeBoolean(true);
                                        ((OptionData) getAnswer()).write(dos);
                                        dos.writeByte(((Byte) getOptionAnswerIndices()).byteValue());
                                } else {
                                        dos.writeBoolean(false);
                                }
                                break;
                        case QuestionDef.QTN_TYPE_LIST_MULTIPLE:
                                if (getAnswer() != null) {
                                        dos.writeBoolean(true);
                                        PersistentHelper.write((Vector) getAnswer(), dos);
                                        Vector col = (Vector) getOptionAnswerIndices();
                                        dos.writeByte(col.size());
                                        for (byte i = 0; i < col.size(); i++) {
                                                dos.writeByte(((Byte) col.elementAt(i)).byteValue());
                                        }
                                } else {
                                        dos.writeBoolean(false);
                                }
                                break;
                        case QuestionDef.QTN_TYPE_REPEAT:
                                if (getAnswer() != null) {
                                        dos.writeBoolean(true);
                                        ((Persistent) getAnswer()).write(dos);
                                } else {
                                        dos.writeBoolean(false);
                                }
                                break;
                        case QuestionDef.QTN_TYPE_IMAGE:
                        case QuestionDef.QTN_TYPE_VIDEO:
                        case QuestionDef.QTN_TYPE_AUDIO:
                                if (answer != null) {
                                        dos.writeBoolean(true);

                                        byte[] bytes = (byte[]) answer;
                                        int len = bytes.length;

                                        dos.writeInt(len);

                                        //The line below results into wiered bugs and therefore
                                        //am commenting it out and replacing it with writting data byte per byte.

                                        //dos.write((byte[])answer,0,((byte[])answer).length);

                                        for (int index = 0; index < len; index++) {
                                                dos.writeByte(bytes[index]);
                                        }
                                } else {
                                        dos.writeBoolean(false);
                                }
                                break;
                }
        }
}

