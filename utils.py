
import uuid
from operator import concat


def generate_file():
    return concat(str(uuid.uuid4()), '.md')