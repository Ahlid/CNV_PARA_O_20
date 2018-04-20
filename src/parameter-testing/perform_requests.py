import requests
import yaml

with open('config.yml', 'r') as f:
    config = yaml.load(f)

url = config['URL']
port = config['port']
maze_endpoint = config['maze_endpoint']
parameter_list = config['params']

for i, params in enumerate(parameter_list):
    request_url = 'http://{}:{}/{}'.format(url, port, maze_endpoint)
    r = requests.get(request_url, params)
    elapsed_secs = r.elapsed.total_seconds()
    print('Request {} took {}s'.format(i+1, elapsed_secs))
