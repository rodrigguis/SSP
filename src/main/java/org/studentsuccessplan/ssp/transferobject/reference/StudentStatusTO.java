package org.studentsuccessplan.ssp.transferobject.reference;

import java.util.List;
import java.util.UUID;

import org.studentsuccessplan.ssp.model.reference.StudentStatus;
import org.studentsuccessplan.ssp.transferobject.TransferObject;

import com.google.common.collect.Lists;

public class StudentStatusTO extends AbstractReferenceTO<StudentStatus>
		implements TransferObject<StudentStatus> {

	public StudentStatusTO() {
		super();
	}

	public StudentStatusTO(UUID id) {
		super(id);
	}

	public StudentStatusTO(UUID id, String name) {
		super(id, name);
	}

	public StudentStatusTO(UUID id, String name, String description) {
		super(id, name, description);
	}

	@Override
	public StudentStatus addToModel(StudentStatus model) {
		super.addToModel(model);
		return model;
	}

	@Override
	public StudentStatus asModel() {
		return addToModel(new StudentStatus());
	}

	public static List<StudentStatusTO> listToTOList(List<StudentStatus> models) {
		List<StudentStatusTO> tos = Lists.newArrayList();
		for (StudentStatus model : models) {
			StudentStatusTO obj = new StudentStatusTO();
			obj.fromModel(model);
			tos.add(obj);
		}
		return tos;
	}

}
