
/* First created by JCasGen Tue Nov 21 11:17:36 CET 2017 */
package de.unihamburg.informatik.nlp4web.tutorial.tut5.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Nov 21 11:17:36 CET 2017
 * @generated */
public class ChunkIOBAnnotation_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ChunkIOBAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.unihamburg.informatik.nlp4web.tutorial.tut5.type.ChunkIOBAnnotation");
 
  /** @generated */
  final Feature casFeat_goldValue;
  /** @generated */
  final int     casFeatCode_goldValue;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getGoldValue(int addr) {
        if (featOkTst && casFeat_goldValue == null)
      jcas.throwFeatMissing("goldValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.ChunkIOBAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_goldValue);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGoldValue(int addr, String v) {
        if (featOkTst && casFeat_goldValue == null)
      jcas.throwFeatMissing("goldValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.ChunkIOBAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_goldValue, v);}
    
  
 
  /** @generated */
  final Feature casFeat_predictValue;
  /** @generated */
  final int     casFeatCode_predictValue;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPredictValue(int addr) {
        if (featOkTst && casFeat_predictValue == null)
      jcas.throwFeatMissing("predictValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.ChunkIOBAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_predictValue);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPredictValue(int addr, String v) {
        if (featOkTst && casFeat_predictValue == null)
      jcas.throwFeatMissing("predictValue", "de.unihamburg.informatik.nlp4web.tutorial.tut5.type.ChunkIOBAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_predictValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ChunkIOBAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_goldValue = jcas.getRequiredFeatureDE(casType, "goldValue", "uima.cas.String", featOkTst);
    casFeatCode_goldValue  = (null == casFeat_goldValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_goldValue).getCode();

 
    casFeat_predictValue = jcas.getRequiredFeatureDE(casType, "predictValue", "uima.cas.String", featOkTst);
    casFeatCode_predictValue  = (null == casFeat_predictValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_predictValue).getCode();

  }
}



    