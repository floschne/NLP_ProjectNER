

/* First created by JCasGen Tue Nov 21 11:17:54 CET 2017 */
package de.unihamburg.informatik.nlp4web.tutorial.tut5.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Nov 21 11:17:54 CET 2017
 * XML source: /Users/seidmuhieyimam/Downloads/de.unihamburg.informatik.nlp4web.tutorial.tut5/src/main/resources/desc/type/GoldNEIOBAnnotation.xml
 * @generated */
public class NEIOBAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(NEIOBAnnotation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected NEIOBAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public NEIOBAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public NEIOBAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public NEIOBAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: goldValue

  /** getter for goldValue - gets 
   * @generated
   * @return value of the feature 
   */
  public String getGoldValue() {
    if (NEIOBAnnotation_Type.featOkTst && ((NEIOBAnnotation_Type)jcasType).casFeat_goldValue == null)
      jcasType.jcas.throwFeatMissing("goldValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.NEIOBAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NEIOBAnnotation_Type)jcasType).casFeatCode_goldValue);}
    
  /** setter for goldValue - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setGoldValue(String v) {
    if (NEIOBAnnotation_Type.featOkTst && ((NEIOBAnnotation_Type)jcasType).casFeat_goldValue == null)
      jcasType.jcas.throwFeatMissing("goldValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.NEIOBAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((NEIOBAnnotation_Type)jcasType).casFeatCode_goldValue, v);}    
   
    
  //*--------------*
  //* Feature: predictValue

  /** getter for predictValue - gets 
   * @generated
   * @return value of the feature 
   */
  public String getPredictValue() {
    if (NEIOBAnnotation_Type.featOkTst && ((NEIOBAnnotation_Type)jcasType).casFeat_predictValue == null)
      jcasType.jcas.throwFeatMissing("predictValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.NEIOBAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NEIOBAnnotation_Type)jcasType).casFeatCode_predictValue);}
    
  /** setter for predictValue - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPredictValue(String v) {
    if (NEIOBAnnotation_Type.featOkTst && ((NEIOBAnnotation_Type)jcasType).casFeat_predictValue == null)
      jcasType.jcas.throwFeatMissing("predictValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.NEIOBAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((NEIOBAnnotation_Type)jcasType).casFeatCode_predictValue, v);}    
  }

    