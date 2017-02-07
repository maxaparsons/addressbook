package com.vaadin.tutorial.addressbook;

import com.vaadin.event.ShortcutAction;
import com.vaadin.tutorial.addressbook.backend.Contact;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.ui.DateField;
import com.vaadin.v7.ui.Grid.SingleSelectionModel;
import com.vaadin.v7.ui.TextField;

/* Create custom UI Components.
 *
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class ContactForm extends FormLayout {

    Button save = new Button("Save", this::save);
    Button cancel = new Button("Cancel", this::cancel);
    Button remove = new Button("Remove", this::remove); //added remove button
    TextField firstName = new TextField("First name");
    TextField lastName = new TextField("Last name");
    TextField task = new TextField("Task");  	//created task, startDate, and expectedEndDate
    DateField startDate = new DateField("Start date"); //fields in the task form
    DateField expectedEndDate = new DateField("Expected end date");
    
    boolean canRemove = false; //flag to determine if an entry has been selected for removal

    Contact contact;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Contact> formFieldBindings;

    public ContactForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        /*
         * Highlight primary actions.
         *
         * With Vaadin built-in styles you can highlight the primary save button
         * and give it a keyboard shortcut for a better UX.
         */
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        setVisible(false);
        
        remove.setStyleName(ValoTheme.BUTTON_PRIMARY); //give "Remove" a style
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel, remove);//add remove button
        actions.setSpacing(true);

        //add new text and date fields 
        addComponents(actions, firstName, lastName, task, startDate, expectedEndDate);
    }

    /*
     * Use any JVM language.
     *
     * Vaadin supports all languages supported by Java Virtual Machine 1.6+.
     * This allows you to program user interface in Java 8, Scala, Groovy or any
     * other language you choose. The new languages give you very powerful tools
     * for organizing your code as you choose. For example, you can implement
     * the listener methods in your compositions or in separate controller
     * classes and receive to various Vaadin component events, like button
     * clicks. Or keep it simple and compact with Lambda expressions.
     */
    public void save(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous service API
            getUI().service.save(contact);

            //changed from name to task in the notifications
            String msg = String.format("Saved '%s'.", contact.getTask());
            Notification.show(msg, Type.TRAY_NOTIFICATION);
            getUI().refreshContacts();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
    }

    public void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        getUI().contactList.select(null);
        getUI().refreshContacts(); //refreshContacts sets the task form visibility to false
    }

    //Functionality for the Remove button
    public void remove(Button.ClickEvent event) {
    	//this selection listener is used to determine if an entry actually has been selected
    	//in order to provide a correct notification
    	getUI().contactList.addSelectionListener(e -> {
    		Object selected = ((SingleSelectionModel) 
    				getUI().contactList.getSelectionModel()).getSelectedRow();
    		
    		if (selected != null) {
    			canRemove = true; //an entry has been selected and can be removed
    		}
    		else
    			canRemove = false; //no entry selected
    	});
    	
    	if (canRemove) { //entry selected
    		getUI().service.delete(contact); //remove entry
    		
    		//notify user of removed task
    		String msg2 = String.format("Removed '%s'.", contact.getTask());
    		Notification.show(msg2, Type.TRAY_NOTIFICATION);
    		
    		//update entries
    		getUI().refreshContacts();
    	}
    	else { //no entry selected
    		//notify user they haven't selected an entry
    		String msg1 = "Please select an item to remove.";
			Notification.show(msg1, Type.TRAY_NOTIFICATION);
    	}
    }
    
    void edit(Contact contact) {
        this.contact = contact;
        if (contact != null) {
            // Bind the properties of the contact POJO to fields in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(contact,
                    this);
            firstName.focus();
        }
        setVisible(contact != null);
    }

    @Override
    public AddressbookUI getUI() {
        return (AddressbookUI) super.getUI();
    }

}
