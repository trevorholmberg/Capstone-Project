# This program is the model that will be trained to identify ASL letters
# Code was written based of this video tutorial: https://www.youtube.com/watch?v=tHL5STNJKag
# @author Matthew Talle


import torch
import torch.nn as nn # specific functions related to neural networks
import torch.optim as optim # used for optimizer
from torch.utils.data import DataLoader
import torchvision.transforms as transforms
from tqdm import tqdm # iterator that prints out a progress bar
from asl_dataset import ASLDataset
from letter_classifier_model import LetterClassifier

# function that resizes all passed in images to same size and converts them to input tensors
transform = transforms.Compose([
    transforms.Resize((128, 128)), # TODO: Needs to be 224x224 for future training
    transforms.ToTensor()])

# needs to start from root directory
data_dir = r"~/Desktop/wcu_cs/capstone/cs496-01-spring-2025-barlowe-repositories-1-mobile_sign_language_app/model/asl_alphabet_train"
test_data_dir = r"~/Desktop/wcu_cs/capstone/cs496-01-spring-2025-barlowe-repositories-1-mobile_sign_language_app/model/to_test"

# creates the dataset objects
dataset = ASLDataset(data_dir, transform)
test_dataset = ASLDataset(test_data_dir, transform)

# wraps each dataset object as a dataloader which handles all processing to parallelize reading each image
dataloader = DataLoader(dataset, batch_size=32, shuffle=True)  # parses dataset into separate batches to feed into model
test_dataloader = DataLoader(test_dataset, batch_size=32, shuffle=True) # used for validation phase


# model
model = LetterClassifier()
model.load_state_dict(torch.load('letter_classifier.pth'))

# Loss function - measures how well the model's predictions match the true target values
criterion = nn.CrossEntropyLoss()
# Optimizer
optimizer = optim.Adam(model.parameters(), lr=0.001)

# Simple training loop. Epoch is the number of iterations through an entire dataset
num_epochs = 5

# loss rates for each phase per epoch
train_losses, val_losses = [], []

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
model.to(device)

for epoch in range(num_epochs):

    # Training phase
    model.train()
    running_loss = 0.0

    # iterate through the training image dataset batch by batch
    for images, labels in tqdm(dataloader, desc='Training loop'):

        # Move inputs and labels to the device
        images, labels = images.to(device), labels.to(device)

        # clears gradients for current iteration
        optimizer.zero_grad()

        # model makes a prediction by calling its forward method
        outputs = model(images)

        # calculates error between predicted match and true target values
        loss = criterion(outputs, labels)

        # calculate gradients - how much the loss function changes with respect to model's weights
        # Indicate how to update weights to further minimize loss
        loss.backward()

        # updates model weights - adjustable values within a model that
        # determine how inputs are transformed to produce outputs
        optimizer.step()

        # loss tracker
        running_loss += loss.item() * labels.size(0)

    # calculates loss rate per epoch
    train_loss = running_loss / len(dataloader.dataset)
    train_losses.append(train_loss)

    # Validation phase
    model.eval()
    running_loss = 0.0

    # iterate through the validation image dataset batch by batch using model with trained weights
    # to help evaluate how well the model is performing on unseen data
    with torch.no_grad():
        for images, labels in tqdm(test_dataloader, desc='Validation loop'):

            # Move inputs and labels to the device
            images, labels = images.to(device), labels.to(device)

            # model makes a prediction by calling its forward method
            outputs = model(images)

            # calculate gradients - how much the loss function changes with respect to model's weights
            # Indicate how to update weights to further minimize loss
            loss = criterion(outputs, labels)

            # loss tracker
            running_loss += loss.item() * labels.size(0)

    # calculates loss rate per epoch
    val_loss = running_loss / len(test_dataloader.dataset)
    val_losses.append(val_loss)
    print(f"Epoch {epoch + 1}/{num_epochs} - Train loss: {train_loss}, Validation loss: {val_loss}")

# saves model at its current state post training
torch.save(model.state_dict(), 'letter_classifier_ver3.pth')