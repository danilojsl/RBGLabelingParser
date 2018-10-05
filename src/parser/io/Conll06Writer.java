package parser.io;

import java.io.IOException;

import parser.DependencyInstance;
import parser.DependencyPipe;
import parser.Options;

class Conll06Writer extends DependencyWriter {
	
	
	Conll06Writer(Options options, DependencyPipe pipe) {
		this.options = options;
		this.labels = pipe.getTypes();
	}
	
	@Override
	public void writeInstance(DependencyInstance gold, int[] predDeps, int[] predLabs) throws IOException {

		
		String[] forms = gold.getForms();
		String[] lemmas = gold.getLemmas();
		String[] cpos = gold.getCpostags();
		String[] pos = gold.getPostags();
		
	    // 3 eles ele pron pron-pers M|3P|NOM 4 SUBJ _ _
	    // ID FORM LEMMA COURSE-POS FINE-POS FEATURES HEAD DEPREL PHEAD PDEPREL
		for (int i = 1, N = gold.getLength(); i < N; ++i) {
			writer.write(i + "\t");
			writer.write(forms[i] + "\t");
			writer.write((lemmas != null && !lemmas[i].equals("") ? lemmas[i] : "_") + "\t");
			writer.write(cpos[i] + "\t");
			writer.write(pos[i] + "\t");
			writer.write("_\t");
			writer.write(predDeps[i] + "\t");
			writer.write(labels[predLabs[i]] + "\t_\t_");
			writer.write("\n");
		}
		
		writer.write("\n");
	}

}
