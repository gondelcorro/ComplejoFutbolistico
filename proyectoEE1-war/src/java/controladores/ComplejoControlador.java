/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import controladores.util.JsfUtil;
import entidadesGestion.Cancha;
import entidadesGestion.Complejo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.event.FileUploadEvent;
import sesionBeansGestion.ComplejoFacadeLocal;

/**
 *
 * @author DelKo
 */
@Named(value = "complejoControlador")
@SessionScoped
public class ComplejoControlador implements Serializable {

    @EJB
    private ComplejoFacadeLocal complejoFacade;
    private List<Complejo> items = null;
    private Complejo selected;
    String nombreImagen;
    private List<Cancha> listaCanchas;
    String nombreComplejo;

    public String getNombreComplejo() {
        return nombreComplejo;
    }

    public void setNombreComplejo(String nombreComplejo) {
        this.nombreComplejo = nombreComplejo;
    }

    public List<Cancha> getListaCanchas() {
        return listaCanchas;
    }

    public void setListaCanchas(List<Cancha> listaCanchas) {
        this.listaCanchas = listaCanchas;
    }

    public String getNombreImagen() {
        return nombreImagen;
    }

    public void setNombreImagen(String nombreImagen) {
        this.nombreImagen = nombreImagen;
    }

    public ComplejoControlador() {
        this.selected = new Complejo();
    }

    public Complejo getSelected() {

        return selected;
    }

    public void setSelected(Complejo selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ComplejoFacadeLocal getFacade() {
        return complejoFacade;
    }

    public Complejo prepareCreate() {
        selected = new Complejo();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {

        try {
            if (selected.getComImg().isEmpty()) {
                selected.setComImg("no-image.png");
            }
        } catch (NullPointerException e) {
            selected.setComImg("no-image.png");
        }
        persist(JsfUtil.PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("ComplejoCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(JsfUtil.PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("ComplejoUpdated"));
    }

    public void destroy() {
        persist(JsfUtil.PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("ComplejoDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Complejo> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(JsfUtil.PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != JsfUtil.PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Complejo getComplejo(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<Complejo> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Complejo> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Complejo.class)
    public static class ComplejoControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ComplejoControlador controller = (ComplejoControlador) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "complejoControlador");
            return controller.getComplejo(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Complejo) {
                Complejo o = (Complejo) object;
                return getStringKey(o.getComId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Complejo.class.getName()});
                return null;
            }
        }
    }

    public void handleFileUpload(FileUploadEvent event) {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        System.out.println("path:" + externalContext.getRealPath("/upload/"));
        nombreImagen = event.getFile().getFileName();
        selected.setComImg(nombreImagen);
        File result = new File(externalContext.getRealPath("/upload/") + File.separator + event.getFile().getFileName());

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(result);
            byte[] buffer = new byte[5120];
            int bulk;
            // Here you get uploaded picture bytes, while debugging you can see that 34818
            InputStream inputStream = event.getFile().getInputstream();
            while (true) {
                bulk = inputStream.read(buffer);
                if (bulk < 0) {
                    break;
                }
                fileOutputStream.write(buffer, 0, bulk);
                fileOutputStream.flush();
            } //end fo while(true)
            fileOutputStream.close();
            inputStream.close();
            FacesMessage msg = new FacesMessage("Información", event.getFile().getFileName() + " fue cargado correctamente");
            FacesContext.getCurrentInstance().addMessage(null, msg);

        } catch (IOException e) {
            FacesMessage error = new FacesMessage("El archivo no ha sido cargado!");
            FacesContext.getCurrentInstance().addMessage(null, error);
        }
    }

    public void canchasPorComplejo(AjaxBehaviorEvent evento) {
        try {
            setListaCanchas(getFacade().buscarPorComplejo(getSelected().getComId()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Aviso", "Error: " + e));
        }
    }

    public void recuperarComplejo(AjaxBehaviorEvent evento) {
        Complejo complejo = new Complejo();
        try {
            complejo = getFacade().find(getSelected().getComId());
            setSelected(complejo);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
