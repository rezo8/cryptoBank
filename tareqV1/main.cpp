#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <cstring>

using namespace std;

void sendCurlRequest(const string& firstName, const string& lastName, const string& email, const string& phoneNumber);

int main() {

    // Initialize variables
    string firstName;
    string lastName;
    string email;
    string phoneNumber;

    // Query the user for their data
    cout << "Welcome to Ribh Bank. "
         << "To make an account, we will need your information. "
         << "Please input your first name and press enter: \n";

    cin >> firstName;
    cout << "Please input your last name and press enter: \n";
    cin >> lastName;
    cout << "Please input your email and press enter: \n";
    cin >> email;
    cout << "Please input your phone number and press enter: \n";
    cin >> phoneNumber;

    // Present the information for verification
    cout << "Type 'yes' and press enter to indicate that your information is correct. If it is not, type any other key.\n";

    cout << "First name: " << firstName << "\n"
         << "Last name: " << lastName << "\n"
         << "Email: " << email << "\n"
         << "Phone number: " << phoneNumber << "\n";

    // Make the function that will send the curl request with the input data
    string yes;
    cin >> yes;
    if (yes == "yes" || yes == "YES" || yes == "Yes") {
        cout << "Welcome to Ribh Bank! Your information has been stored.";
        // Send data to the server using curl request
        sendCurlRequest(firstName, lastName, email, phoneNumber);
    }
    else {
        cout << "Please run the program again and input your information correctly";
        return 0;
    }
}

void sendCurlRequest(const string& firstName, const string& lastName, const string& email, const string& phoneNumber) {

    // Create the JSON data string
    string jsonData = "{\"firstName\": \"" + firstName + "\", "
                      "\"lastName\": \"" + lastName + "\", "
                      "\"email\": \"" + email + "\", "
                      "\"phoneNumber\": \"" + phoneNumber + "\"}";

    // Create the full curl command with headers and JSON data
    string curlCommand = "curl --location 'localhost:8080/users' "
                         "--header 'Content-Type: application/json' "
                         "--data-raw '" + jsonData + "'";

    // Use popen() to capture the output of the curl command
    FILE* pipe = popen(curlCommand.c_str(), "r");
    if (!pipe) {
        cerr << "Failed to run command: " << strerror(errno) << endl;
        return;
    }

    // Read the output from the pipe
    char read[128];
    while (fgets(read, sizeof(read), pipe) != nullptr) {
        cout << "Output from command: " << read;
    }

    // Close the pipe
    pclose(pipe);
}
