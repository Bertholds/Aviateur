/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.sun.xml.rpc.processor.modeler.j2ee.xml.paramValueType;
import entity.Aeroport;
import entity.Appareil;
import entity.Escale;
import entity.EscalePK;
import entity.Pays;
import entity.Personnel;
import entity.Ville;
import entity.Vol;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.convert.Converter;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DualListModel;
import sessions.AeroportFacadeLocal;
import sessions.AppareilFacadeLocal;
import sessions.EscaleFacadeLocal;
import sessions.PaysFacadeLocal;
import sessions.PersonnelFacadeLocal;
import sessions.VilleFacadeLocal;
import sessions.VolFacadeLocal;
import utility.DateUtils;
import utility.MessageView;
import utility.MethodUtil;

/**
 *
 * @author bkndj
 */
public class VolController implements Serializable, Converter {

    private List<Vol> vols = new ArrayList<>();

    private DualListModel<Personnel> personnels = new DualListModel<>(new ArrayList<>(), new ArrayList<>());

    private List<Appareil> appareils = new ArrayList<>();

    private List<Pays> paysesDepart = new ArrayList<>();
    private List<Pays> paysesArriver = new ArrayList<>();

    private List<Ville> villesDepart = new ArrayList<>();
    private List<Ville> villesArriver = new ArrayList<>();

    private List<Aeroport> aeroportsDepart = new ArrayList<>();
    private List<Aeroport> aeroportsArriver = new ArrayList<>();
    private List<Aeroport> aeroportMetaData = new ArrayList<>();

    private List<Personnel> selectedPersonnels;
    private List<Escale> selectedEscales = new ArrayList<>();

    private Vol selectedVol;
    private Escale selectedEscale;

    private Integer idAppareil;

    private Integer idVilleDepart;
    private Integer idPaysDepart;
    private Integer idAeroportDepart;

    private Integer idVilleArriver;
    private Integer idPaysArriver;
    private Integer idAeroportArriver;
    private Integer idAeroportMetaData;

    private String heureVolJour;
    private String heureVolNuit;
    private String heureDepart;
    private String heureArriver;

    private String action;

    @EJB
    private VolFacadeLocal volRepository;
    @EJB
    private PersonnelFacadeLocal personnelRepository;
    @EJB
    private AppareilFacadeLocal appareilRepository;
    @EJB
    private PaysFacadeLocal paysRepository;
    @EJB
    private VilleFacadeLocal villeRepository;
    @EJB
    private AeroportFacadeLocal aeroportRepository;
    @EJB
    private EscaleFacadeLocal escaleRepository;

    Map<String, String> params;
    private boolean isEdit = false;

    public VolController() {
        PrimeFaces.current().ajax().update("secondary_form:save");
    }

    @PostConstruct
    private void initialize() {

        this.vols.clear();
        this.vols.addAll(volRepository.findAllOrderDesc());

        params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        if (params.get("edit") != null) {
            initVolEdit();
        }

        /*if (params.get("edited") != null) {
            System.out.println("not nullll");
            PrimeFaces.current().ajax().update("vol_manage_form:msg");
            MessageView.info("La modification du vol ID " + params.get("edited") + "à réussie");
            MethodUtil.addMessage(FacesMessage.SEVERITY_INFO, "Opération successfull", "L'enregistrement a réussie");
        }*/
    }

    private void initVolEdit() {
        this.chargeListPersonnel();

        selectedEscales.clear();

        this.appareils.clear();
        this.appareils.addAll(this.appareilRepository.findAll());

        this.paysesDepart.clear();
        this.paysesDepart.addAll(this.paysRepository.findAllOrderAsc());  

        this.paysesArriver.clear();
        this.paysesArriver.addAll(this.paysRepository.findAllOrderAsc());

        this.villesDepart.clear();
        this.villesDepart.addAll(this.villeRepository.findAll());

        this.villesArriver.clear();
        this.villesArriver.addAll(this.villeRepository.findAll());

        List<Aeroport> aeroports = this.aeroportRepository.findAllAsc();
        this.aeroportsDepart.clear();
        this.aeroportsDepart.addAll(aeroports);

        this.aeroportsArriver.clear();
        this.aeroportsArriver.addAll(aeroports);

        this.aeroportMetaData.clear();
        this.aeroportMetaData.addAll(aeroports);

        String editingNumber = params.get("edit");
        if (Integer.parseInt(editingNumber) != 0) {
            System.out.println("params: " + params.get("edit"));
            this.isEdit = true;

            this.selectedVol = volRepository.find(Integer.parseInt(params.get("edit")));
            this.selectedPersonnels = (List<Personnel>) this.selectedVol.getPersonnelCollection();
            this.selectedEscales = (List<Escale>) this.selectedVol.getEscaleCollection();

            //Les liste déroulante (p:selectOneMenu) nécéssite des affectations sur leurs
            //attribut value pour q'automatiquement le bon item soit selectionné 
            //infos primaire
            this.idAppareil = this.selectedVol.getIdappareil().getIdappareil();

            //infos secondaire
            this.idPaysDepart = this.selectedVol.getIdaeroport().getIdville().getIdpays().getIdpays();
            this.idPaysArriver = this.selectedVol.getAerIdaeroport().getIdville().getIdpays().getIdpays();

            this.idVilleDepart = this.selectedVol.getIdaeroport().getIdville().getIdville();
            this.idVilleArriver = this.selectedVol.getAerIdaeroport().getIdville().getIdville();

            this.idAeroportDepart = this.selectedVol.getIdaeroport().getIdaeroport();
            this.idAeroportArriver = this.selectedVol.getIdaeroport().getIdaeroport();

            this.heureVolJour = DateUtils.getDateOnString(this.selectedVol.getHeurevoljour(), "HH:mm");
            this.heureVolNuit = DateUtils.getDateOnString(this.selectedVol.getHeurevolnuit(), "HH:mm");

            //Personnel navigant
            getPersonnelNavigant();
        }
    }

    public void chargeListPersonnel() {
        personnels.getSource().clear();
        personnels.getSource().addAll(personnelRepository.findAll());
    }

    private void getPersonnelNavigant() {
        this.personnels.getTarget().clear();
        this.personnels.getTarget().addAll(this.selectedPersonnels);
        this.personnels.getSource().removeAll(this.selectedPersonnels);
    }

    public void openNew() {
        if (this.selectedVol == null) {
            this.selectedVol = new Vol();
            System.out.println("Open new");
        }
    }

    public void instantiateEscale() {
        if (this.selectedEscale == null) {
            this.selectedEscale = new Escale();
            System.out.println("Open new escale");
        }
    }

    public void defineVolAppareil() {
        Appareil appareil = appareilRepository.find(this.idAppareil);
        this.selectedVol.setIdappareil(appareil);
    }

    public void populateSiblingCity(String country) {
        System.out.println("country: " + country);
        if (country.equals("depart")) {
            Pays pays = paysRepository.find(this.idPaysDepart);
            this.villesDepart.clear();
            this.villesDepart.addAll(this.villeRepository.findByPays(pays));
        } else {
            Pays pays = paysRepository.find(this.idPaysArriver);
            this.villesArriver.clear();
            this.villesArriver.addAll(this.villeRepository.findByPays(pays));
        }
    }

    public void populateSiblingAeroplane(String city) {
        if (city.equals("depart")) {
            Ville ville = villeRepository.find(this.idVilleDepart);
            this.aeroportsDepart.clear();
            this.aeroportsDepart.addAll(this.aeroportRepository.findByVille(ville));
        } else {
            Ville ville = villeRepository.find(this.idVilleArriver);
            this.aeroportsArriver.clear();
            this.aeroportsArriver.addAll(this.aeroportRepository.findByVille(ville));
        }
    }

    public String save() {
        int createdVolId = 0;
        //String result = null;

        PrimeFaces.current().ajax().update("form_notification:msg");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Flash flash = facesContext.getExternalContext().getFlash();
        flash.setKeepMessages(true);
        flash.setRedirect(true);

        try {
            System.out.println("edit: " + isEdit);
            this.selectedVol.setEscaleCollection(selectedEscales);

            if (isEdit) {
                System.out.println("ID vol: " + this.selectedVol.getIdvol());
                this.volRepository.edit(selectedVol);
                // result = "vol.xhtml?edited=" + this.selectedVol.getIdvol() + "&faces-redirect=true";
            } else {
                this.volRepository.create(selectedVol);
                createdVolId = volRepository.maxVolId();
                //  result = "vol.xhtml?created=" + createdVolId + "&faces-redirect=true";
            }

            if (isEdit) {
                MessageView.info("Le vol avec ID " + selectedVol.getIdvol() + " a été modifier avec success");
            } else {
                MessageView.info("Le vol avec ID " + createdVolId + " a été créer avec success");
            }

        } catch (Exception e) {
            e.printStackTrace();
            MethodUtil.addMessage(FacesMessage.SEVERITY_ERROR, "Attention", "Une érreur est survenu " + e.getMessage());
        } finally {
            initialize();
        }
        return "vol.xhtml?faces-redirect=true";
    }

    public void primarySave() {
        PrimeFaces.current().executeScript("PF('dialog_primary').hide()");
    }

    public void secondarySave() {
        Aeroport aeroportDepart = this.aeroportRepository.find(this.idAeroportDepart);
        Aeroport aeroportArriver = this.aeroportRepository.find(this.idAeroportArriver);

        this.selectedVol.setIdaeroport(aeroportDepart);
        this.selectedVol.setAerIdaeroport(aeroportArriver);


        /*int t = 135;
        int hours = t / 60;   // since both are ints, you get an int
        int minutes = t % 60;
        System.out.println(String.format("%d:%02d", hours, minutes));*/
        this.selectedVol.setHeurevoljour(DateUtils.getDate(heureVolJour, "HH:mm"));
        this.selectedVol.setHeurevolnuit(DateUtils.getDate(heureVolNuit, "HH:mm"));
        System.out.println("HVJ: " + heureVolJour);
        PrimeFaces.current().executeScript("PF('dialog_secondary').hide()");
    }

    public void tertiarySave() {
        this.selectedPersonnels = personnels.getTarget();
        this.selectedVol.setPersonnelCollection(selectedPersonnels);
        PrimeFaces.current().executeScript("PF('dialog_tertiary').hide()");
    }

    public void quatenarySave() {
        System.out.println("quatenarySave");
        System.out.println("Heure test: ");

        this.selectedEscale.setHeureArriver(DateUtils.getDate(this.heureArriver, "HH:mm"));
        this.selectedEscale.setHeureDepart(DateUtils.getDate(this.heureDepart, "HH:mm"));
        this.selectedEscale.setAeroport(this.aeroportRepository.find(this.idAeroportMetaData));
        this.selectedEscale.setVol(selectedVol);
        
        if(this.isEdit){
            this.selectedEscale.setEscalePK(new EscalePK((this.selectedVol.getIdvol()), idAeroportMetaData));
        }else{
            this.selectedEscale.setEscalePK(new EscalePK((this.volRepository.maxVolId() + 1), idAeroportMetaData));
        }

        this.selectedEscales.add(selectedEscale);

        System.out.println("result: " + selectedEscale.toString());
        PrimeFaces.current().executeScript("PF('dialog_quatenary').hide()");
        this.selectedEscale = null;
    }

    public boolean hasDisabledShowListPersonnelNavigantBtn() {
        return this.selectedPersonnels != null && !this.selectedPersonnels.isEmpty();
    }

    public boolean hasDisabledShowListEscaleAeroportBtn() {
        return this.selectedEscales != null && !this.selectedEscales.isEmpty();
    }

    public boolean hasDisabledBtnSave() {
        if (this.selectedVol != null) {
            return !this.selectedVol.checkIfHasOwnPropertyIsNull();
        }
        return true;
    }

    public boolean hasSelectedVol() {
        return this.selectedVol != null;
    }

    public boolean hasEdit() {
        return this.isEdit;
    }

    public void delete() {

        try {
            volRepository.remove(this.selectedVol);
            MessageView.info("La suppression du vol ID " + this.selectedVol.getIdvol() + " a réussie");
        } catch (Exception e) {
            MethodUtil.addMessage(FacesMessage.SEVERITY_ERROR, "Opération échouer", "La suppression a échouer");
            e.printStackTrace();
        } finally {
            initialize();
            PrimeFaces.current().ajax().update("vol_manage_form:messages");
            PrimeFaces.current().ajax().update("form_notification:msg");
            PrimeFaces.current().ajax().update("vol_manage_form:dt-vols");
        }
    }

    public void deleteSelectedEscale(Escale escale) {
        this.selectedEscales.remove(escale);
        System.out.println("delete escale");
    }

    public void onRowEdit(RowEditEvent<Escale> event) {

        Escale escale = event.getObject();
        this.selectedEscales.remove(escale);
        escale.setAeroport(this.aeroportRepository.find(this.idAeroportMetaData));
        escale.setEscalePK(new EscalePK(escale.getEscalePK().getIdvol(), this.idAeroportMetaData));
        this.selectedEscales.add(escale);

        System.out.println("editeddd");
        PrimeFaces.current().ajax().update("show_list_escale_form:dt-escales");

        FacesMessage msg = new FacesMessage("Edited successful", String.valueOf(event.getObject().getAeroport().getNom()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent<Escale> event) {
        FacesMessage msg = new FacesMessage("Cancelled successful", String.valueOf(event.getObject().getAeroport().getNom()));
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Vol> getVols() {
        return vols;
    }

    public void setVols(List<Vol> vols) {
        this.vols = vols;
    }

    public DualListModel<Personnel> getPersonnels() {
        return personnels;
    }

    public void setPersonnels(DualListModel<Personnel> personnels) {
        this.personnels = personnels;
    }

    public List<Escale> getSelectedEscales() {
        return selectedEscales;
    }

    public void setSelectedEscales(List<Escale> selectedEscale) {
        this.selectedEscales = selectedEscale;
    }

    public List<Personnel> getSelectedPersonnels() {
        return selectedPersonnels;
    }

    public void setSelectedPersonnels(List<Personnel> selectedPersonnels) {
        this.selectedPersonnels = selectedPersonnels;
    }

    public Vol getSelectedVol() {
        return selectedVol;
    }

    public void setSelectedVol(Vol selectedVol) {
        this.selectedVol = selectedVol;
    }

    public Escale getSelectedEscale() {
        return selectedEscale;
    }

    public void setSelectedEscale(Escale selectedEscale) {
        this.selectedEscale = selectedEscale;
    }

    public List<Appareil> getAppareils() {
        return appareils;
    }

    public void setAppareils(List<Appareil> appareils) {
        this.appareils = appareils;
    }

    public Integer getIdVilleDepart() {
        return idVilleDepart;
    }

    public void setIdVilleDepart(Integer idVilleDepart) {
        this.idVilleDepart = idVilleDepart;
    }

    public Integer getIdPaysDepart() {
        return idPaysDepart;
    }

    public void setIdPaysDepart(Integer idPaysDepart) {
        this.idPaysDepart = idPaysDepart;
    }

    public Integer getIdAeroportDepart() {
        return idAeroportDepart;
    }

    public void setIdAeroportDepart(Integer idAeroportDepart) {
        this.idAeroportDepart = idAeroportDepart;
    }

    public Integer getIdVilleArriver() {
        return idVilleArriver;
    }

    public void setIdVilleArriver(Integer idVilleArriver) {
        this.idVilleArriver = idVilleArriver;
    }

    public Integer getIdPaysArriver() {
        return idPaysArriver;
    }

    public void setIdPaysArriver(Integer idPaysArriver) {
        this.idPaysArriver = idPaysArriver;
    }

    public Integer getIdAeroportArriver() {
        return idAeroportArriver;
    }

    public void setIdAeroportArriver(Integer idAeroportArriver) {
        this.idAeroportArriver = idAeroportArriver;
    }

    public Integer getIdAeroportMetaData() {
        return idAeroportMetaData;
    }

    public void setIdAeroportMetaData(Integer idAeroportMetaData) {
        this.idAeroportMetaData = idAeroportMetaData;
    }

    public Integer getIdAppareil() {
        return idAppareil;
    }

    public void setIdAppareil(Integer idAppareil) {
        this.idAppareil = idAppareil;
    }

    public List<Pays> getPaysesDepart() {
        return paysesDepart;
    }

    public void setPaysesDepart(List<Pays> paysesDepart) {
        this.paysesDepart = paysesDepart;
    }

    public List<Pays> getPaysesArriver() {
        return paysesArriver;
    }

    public void setPaysesArriver(List<Pays> paysesArriver) {
        this.paysesArriver = paysesArriver;
    }

    public List<Ville> getVillesDepart() {
        return villesDepart;
    }

    public void setVillesDepart(List<Ville> villesDepart) {
        this.villesDepart = villesDepart;
    }

    public List<Ville> getVillesArriver() {
        return villesArriver;
    }

    public void setVillesArriver(List<Ville> villesArriver) {
        this.villesArriver = villesArriver;
    }

    public List<Aeroport> getAeroportsDepart() {
        return aeroportsDepart;
    }

    public void setAeroportsDepart(List<Aeroport> aeroportsDepart) {
        this.aeroportsDepart = aeroportsDepart;
    }

    public List<Aeroport> getAeroportsArriver() {
        return aeroportsArriver;
    }

    public void setAeroportsArriver(List<Aeroport> aeroportsArriver) {
        this.aeroportsArriver = aeroportsArriver;
    }

    public List<Aeroport> getAeroportMetaData() {
        return aeroportMetaData;
    }

    public void setAeroportMetaData(List<Aeroport> aeroportMetaData) {
        this.aeroportMetaData = aeroportMetaData;
    }

    public String getHeureVolJour() {
        return heureVolJour;
    }

    public void setHeureVolJour(String heureVolJour) {
        this.heureVolJour = heureVolJour;
    }

    public String getHeureVolNuit() {
        return heureVolNuit;
    }

    public void setHeureVolNuit(String heureVolNuit) {
        this.heureVolNuit = heureVolNuit;
    }

    public String getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(String heureDepart) {
        this.heureDepart = heureDepart;
    }

    public String getHeureArriver() {
        return heureArriver;
    }

    public void setHeureArriver(String heureArriver) {
        this.heureArriver = heureArriver;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return personnelRepository.find(Integer.parseInt(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Personnel) value).getIdpersonnel().toString();
    }

}
