from flask import Blueprint

chef = Blueprint('chef', __name__)

from . import routes
