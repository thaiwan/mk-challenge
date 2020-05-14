import React from 'react';
import ReactDOM from 'react-dom';
import { Form, Field } from 'react-final-form';
import { TextField } from 'final-form-material-ui';
import {
  Typography,
  Paper,
  Grid,
  Button,
  CssBaseline,
} from '@material-ui/core';
import axios from 'axios';

const headers = {
  'Content-Type': 'application/json'
}


const onSubmit = async values => {

  axios.post('https://dxaxbj4z11.execute-api.us-west-1.amazonaws.com/test/email/', JSON.stringify(values), { headers: headers })
      .then((result) => { window.alert(JSON.stringify(result.data)) },
          (error) => { window.alert(error) }
      );
};
const validate = values => {
  const errors = {};
  if (!values.name) {
    errors.name = 'Required';
  }
  if (!values.message) {
    errors.message = 'Required';
  }
  if (!values.email) {
    errors.email = 'Required';
  }
  return errors;
};

function App() {
  return (
      <div style={{ padding: 16, margin: 'auto', maxWidth: 600 }}>
        <CssBaseline />
        <Typography variant="h4" align="center" component="h1" gutterBottom>
          Email Sending Form
        </Typography>
        <Form
            onSubmit={onSubmit}
            validate={validate}
            render={({ handleSubmit, reset, submitting, pristine, values }) => (
                <form onSubmit={handleSubmit} noValidate>
                  <Paper style={{ padding: 16 }}>
                    <Grid container alignItems="flex-start" spacing={2}>
                      <Grid item xs={6}>
                        <Field
                            fullWidth
                            required
                            name="name"
                            component={TextField}
                            type="text"
                            label="Name"
                        />
                      </Grid>
                      <Grid item xs={6}>
                        <Field
                            fullWidth
                            required
                            name="email"
                            component={TextField}
                            type="email"
                            label="Email"
                        />
                      </Grid>
                      <Grid item xs={12}>
                        <Field
                            name="message"
                            fullWidth
                            required
                            component={TextField}
                            type="text"
                            label="Message"
                        />
                      </Grid>
                      <Grid item style={{ marginTop: 16 }}>
                        <Button
                            type="button"
                            variant="contained"
                            onClick={reset}
                            disabled={submitting || pristine}
                        >
                          Reset
                        </Button>
                      </Grid>
                      <Grid item style={{ marginTop: 16 }}>
                        <Button
                            variant="contained"
                            color="primary"
                            type="submit"
                            disabled={submitting}
                        >
                          Submit
                        </Button>
                      </Grid>
                    </Grid>
                  </Paper>
                </form>
            )}
        />
      </div>
  );
}

ReactDOM.render(<App />, document.querySelector('#root'));