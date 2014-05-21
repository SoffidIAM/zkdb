package es.caib.zkib.sample;

import java.util.Calendar;
import java.util.Date;

public class Title {
	public String name;
	public Date date = new Date ();
	public Calendar calendar = Calendar.getInstance();
	
	public String getName() { return name; }

	public void setName(String name) {
		this.name = name;
	}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

}
