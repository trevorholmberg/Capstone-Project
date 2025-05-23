# This program was made to deploy the model
# @author Matthew Talle


import torch
from torch.utils.mobile_optimizer import optimize_for_mobile
from letter_classifier_model import LetterClassifier

# Initialize Model and load the saved state post training
model = LetterClassifier()
model.load_state_dict(torch.load('letter_classifier_ver2.pth'))

model.eval() # set to evaluation mode

# Export pytorch model to TorchScript which is optimized for mobile and can be loaded in Android
exported_model = torch.jit.script(model)

# Saves the exported model's current state which I then manually add to the assets folder
exported_model._save_for_lite_interpreter("letter_classifier_ver3.ptl")
