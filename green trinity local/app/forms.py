from flask_wtf import FlaskForm
from wtforms import StringField, PasswordField, SubmitField, TextAreaField, SelectField
from wtforms.validators import DataRequired, EqualTo, ValidationError, Length
from flask_wtf.file import FileField, FileAllowed
from app.models import User

class RegistrationForm(FlaskForm):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    confirm_password = PasswordField('Confirm Password', validators=[DataRequired(), EqualTo('password')])
    submit = SubmitField('Register')

    def validate_username(self, username):
        user = User.query.filter_by(username=username.data).first()
        if user:
            raise ValidationError('That username is already taken.')

class LoginForm(FlaskForm):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    submit = SubmitField('Login')

class ChatMessageForm(FlaskForm):
    message = TextAreaField('Message', validators=[DataRequired(), Length(min=1, max=200)])
    submit = SubmitField('Send')

class TaskProofForm(FlaskForm):
    proof = FileField('Proof of Completion', validators=[FileAllowed(['jpg', 'png', 'pdf', 'txt'])])
    submit = SubmitField('Upload')

class PresenceForm(FlaskForm):
    submit = SubmitField('Mark as Present')

class TaskAssignmentForm(FlaskForm):
    employee = SelectField('Employee', validators=[DataRequired()])
    description = TextAreaField('Task Description', validators=[DataRequired(), Length(min=5, max=200)])
    submit = SubmitField('Assign Task')

class AccessControlForm(FlaskForm):
    employee = SelectField('Employee', validators=[DataRequired()])
    department = SelectField('Department', choices=["General", "Engineering", "Analytics", "Security", "HR"], validators=[DataRequired()])
    submit = SubmitField('Update Access')

class EmployeeForm(FlaskForm):
    """Form for adding/editing employees in HR Portal"""
    name = StringField('Full Name', validators=[DataRequired(), Length(min=2, max=100)])
    role = SelectField('Role', choices=[('Employee', 'Employee'), ('Chef', 'Chef')], validators=[DataRequired()])
    status = SelectField('Status', choices=[('Active', 'Active'), ('Absent', 'Absent')], validators=[DataRequired()])
    contact_info = StringField('Contact Info', validators=[Length(max=200)], 
                              render_kw={'placeholder': 'Email, phone, etc.'})
    submit = SubmitField('Save Employee')