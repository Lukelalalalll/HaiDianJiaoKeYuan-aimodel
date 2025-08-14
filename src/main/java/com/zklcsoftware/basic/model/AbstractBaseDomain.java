package com.zklcsoftware.basic.model;

import java.io.Serializable;

public abstract class AbstractBaseDomain  implements Serializable{
	
	// Validation groups
	public interface AddValid {};
	public interface UpdateValid {};
	
	// JsonView seletors
	public interface WithoutView {};
	public interface WithView extends WithoutView {};
}
